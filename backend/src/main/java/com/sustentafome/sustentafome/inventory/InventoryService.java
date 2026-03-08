package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.LoteProducaoRepository;
import com.sustentafome.sustentafome.production.Product;
import com.sustentafome.sustentafome.production.ProductRepository;
import com.sustentafome.sustentafome.common.OperationalMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ArmazemRepository armazemRepository;
    private final ItemEstoqueRepository itemRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final InventarioCiclicoRepository inventarioCiclicoRepository;
    private final ProductRepository productRepository;
    private final LoteProducaoRepository loteRepository;
    private final OperationalMetrics operationalMetrics;

    public Page<ItemEstoque> listEstoque(int page,
                                         int size,
                                         Long produtoId,
                                         Long armazemId,
                                         LocalDate validadeDe,
                                         LocalDate validadeAte,
                                         Boolean somenteDisponivel) {
        Specification<ItemEstoque> spec = Specification.where(null);

        if (produtoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("produto").get("id"), produtoId));
        }
        if (armazemId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("armazem").get("id"), armazemId));
        }
        if (validadeDe != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataValidade"), validadeDe));
        }
        if (validadeAte != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataValidade"), validadeAte));
        }
        if (Boolean.TRUE.equals(somenteDisponivel)) {
            spec = spec.and((root, query, cb) -> {
                var reservado = cb.coalesce(root.get("reservado"), cb.literal(BigDecimal.ZERO));
                var bloqueado = cb.coalesce(root.get("bloqueado"), cb.literal(BigDecimal.ZERO));
                var disponivel = cb.diff(cb.diff(root.get("quantidade"), reservado), bloqueado);
                return cb.greaterThan(disponivel, BigDecimal.ZERO);
            });
        }

        return itemRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(MovimentacaoRequest request) {
        Timer.Sample sample = Timer.start();
        try {
            var armazem = armazemRepository.findById(request.armazemId())
                    .orElseThrow(() -> new IllegalArgumentException("Armazem nao encontrado"));
            Product produto = productRepository.findById(request.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
            LoteProducao lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;

            ItemEstoque item;
            if (request.tipo() == MovementType.SAIDA) {
                item = selecionarItemDisponivel(produto, armazem, lote, request.quantidade());
            } else {
                item = getOrCreateItem(armazem, produto, lote, request.unidadeMedida());
            }

            BigDecimal delta = switch (request.tipo()) {
                case ENTRADA -> request.quantidade();
                case SAIDA -> request.quantidade().negate();
                case AJUSTE -> request.quantidade(); // pode ser negativo ou positivo
            };

            if (delta.signum() < 0) {
                validarDisponibilidade(item, delta.abs());
                consumirLocks(item, delta.abs());
            }
            item.setQuantidade(item.getQuantidade().add(delta));
            aplicarMetadadosDeLote(item, lote);
            itemRepository.save(item);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setArmazem(armazem);
            mov.setProduto(produto);
            mov.setLote(lote);
            mov.setTipo(request.tipo());
            mov.setQuantidade(request.quantidade());
            mov.setMotivo(request.motivo());
            mov.setDataMovimentacao(LocalDateTime.now());
            return movimentacaoRepository.save(mov);
        } finally {
            operationalMetrics.recordPicking(sample);
        }
    }

    @Transactional
    public ReservaEstoque reservar(ReservaRequest request) {
        Timer.Sample sample = Timer.start();
        Armazem armazem = null;
        if (request.armazemId() != null) {
            armazem = armazemRepository.findById(request.armazemId())
                    .orElseThrow(() -> new IllegalArgumentException("Armazem nao encontrado"));
        }
        Product produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        LoteProducao lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;

        ItemEstoque item = selecionarItemDisponivel(produto, armazem, lote, request.quantidade());
        item.setReservado(item.getReservado().add(request.quantidade()));
        itemRepository.save(item);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setArmazem(item.getArmazem());
        reserva.setProduto(produto);
        reserva.setLote(lote);
        reserva.setQuantidade(request.quantidade());
        reserva.setStatus(ReservaStatus.ATIVA);
        reserva.setDataReserva(LocalDateTime.now());
        try {
            return reservaRepository.save(reserva);
        } finally {
            operationalMetrics.recordPicking(sample);
        }
    }

    @Transactional
    public ReservaEstoque bloquearReserva(Long reservaId) {
        ReservaEstoque reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva nao encontrada"));
        if (reserva.getStatus() != ReservaStatus.ATIVA) {
            throw new IllegalStateException("Reserva precisa estar ATIVA para bloqueio");
        }
        ItemEstoque item = itemRepository.findByProdutoAndLoteAndArmazem(
                        reserva.getProduto(), reserva.getLote(), reserva.getArmazem())
                .orElseThrow(() -> new IllegalArgumentException("Item do estoque nao encontrado para reserva"));

        validarReservadoSuficiente(item, reserva.getQuantidade());
        item.setReservado(item.getReservado().subtract(reserva.getQuantidade()));
        item.setBloqueado(item.getBloqueado().add(reserva.getQuantidade()));
        itemRepository.save(item);

        reserva.setStatus(ReservaStatus.BLOQUEADA);
        return reservaRepository.save(reserva);
    }

    @Transactional
    public TransferenciaResponse transferir(TransferenciaRequest request) {
        if (request.armazemOrigemId().equals(request.armazemDestinoId())) {
            throw new IllegalArgumentException("Origem e destino devem ser diferentes");
        }
        var origem = armazemRepository.findById(request.armazemOrigemId())
                .orElseThrow(() -> new IllegalArgumentException("Armazem de origem nao encontrado"));
        var destino = armazemRepository.findById(request.armazemDestinoId())
                .orElseThrow(() -> new IllegalArgumentException("Armazem de destino nao encontrado"));
        var produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        var lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;

        // 1) Bloquear no armazem de origem (garante separacao)
        ItemEstoque itemOrigem = selecionarItemDisponivel(produto, origem, lote, request.quantidade());
        itemOrigem.setBloqueado(itemOrigem.getBloqueado().add(request.quantidade()));
        aplicarMetadadosDeLote(itemOrigem, lote);
        itemRepository.save(itemOrigem);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setArmazem(origem);
        reserva.setProduto(produto);
        reserva.setLote(lote);
        reserva.setQuantidade(request.quantidade());
        reserva.setStatus(ReservaStatus.BLOQUEADA);
        reserva.setDataReserva(LocalDateTime.now());
        ReservaEstoque reservaSalva = reservaRepository.save(reserva);

        // 2) Saida do estoque origem
        BigDecimal quantidade = request.quantidade();
        validarDisponibilidade(itemOrigem, quantidade);
        consumirLocks(itemOrigem, quantidade);
        itemOrigem.setQuantidade(itemOrigem.getQuantidade().subtract(quantidade));
        itemRepository.save(itemOrigem);

        MovimentacaoEstoque movSaida = new MovimentacaoEstoque();
        movSaida.setArmazem(origem);
        movSaida.setProduto(produto);
        movSaida.setLote(lote);
        movSaida.setTipo(MovementType.SAIDA);
        movSaida.setQuantidade(quantidade);
        movSaida.setMotivo(request.motivo() == null ? "TRANSFERENCIA" : request.motivo());
        movSaida.setDataMovimentacao(LocalDateTime.now());
        movSaida = movimentacaoRepository.save(movSaida);

        // 3) Entrada no destino
        ItemEstoque itemDestino = getOrCreateItem(destino, produto, lote, itemOrigem.getUnidadeMedida());
        itemDestino.setQuantidade(itemDestino.getQuantidade().add(quantidade));
        aplicarMetadadosDeLote(itemDestino, lote);
        itemRepository.save(itemDestino);

        MovimentacaoEstoque movEntrada = new MovimentacaoEstoque();
        movEntrada.setArmazem(destino);
        movEntrada.setProduto(produto);
        movEntrada.setLote(lote);
        movEntrada.setTipo(MovementType.ENTRADA);
        movEntrada.setQuantidade(quantidade);
        movEntrada.setMotivo(request.motivo() == null ? "TRANSFERENCIA" : request.motivo());
        movEntrada.setDataMovimentacao(LocalDateTime.now());
        movEntrada = movimentacaoRepository.save(movEntrada);

        reservaSalva.setStatus(ReservaStatus.CONSUMIDA);
        reservaRepository.save(reservaSalva);

        return new TransferenciaResponse(reservaSalva, movSaida, movEntrada);
    }

    @Transactional
    public InventarioCiclico registrarContagemCiclica(ContagemCiclicaRequest request) {
        var armazem = armazemRepository.findById(request.armazemId())
                .orElseThrow(() -> new IllegalArgumentException("Armazem nao encontrado"));
        var produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        var lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;

        ItemEstoque item = null;
        if (lote != null) {
            item = itemRepository.findByProdutoAndLoteAndArmazem(produto, lote, armazem).orElse(null);
        }
        if (item == null) {
            var candidatos = itemRepository.findDisponiveisFefoPeps(produto, armazem);
            if (!candidatos.isEmpty()) {
                item = candidatos.get(0);
            }
        }

        BigDecimal quantidadeEsperada = item == null ? BigDecimal.ZERO
                : disponivel(item);
        BigDecimal diferenca = request.quantidadeContada().subtract(quantidadeEsperada);

        if (diferenca.compareTo(BigDecimal.ZERO) != 0) {
            MovimentacaoRequest movRequest = new MovimentacaoRequest(
                    armazem.getId(),
                    produto.getId(),
                    lote != null ? lote.getId() : null,
                    MovementType.AJUSTE,
                    diferenca,
                    item != null ? item.getUnidadeMedida() : null,
                    "AJUSTE_INVENTARIO_CICLICO"
            );
            registrarMovimentacao(movRequest);
        }

        InventarioCiclico inventario = new InventarioCiclico();
        inventario.setArmazem(armazem);
        inventario.setProduto(produto);
        inventario.setLote(lote);
        inventario.setDataReferenciaSemana(LocalDate.now().with(DayOfWeek.MONDAY));
        inventario.setQuantidadeEsperada(quantidadeEsperada);
        inventario.setQuantidadeContada(request.quantidadeContada());
        inventario.setDiferenca(diferenca);
        inventario.setObservacao(request.observacao());
        inventario.setStatus(diferenca.compareTo(BigDecimal.ZERO) == 0 ? InventarioCiclicoStatus.ABERTO : InventarioCiclicoStatus.AJUSTADO);
        return inventarioCiclicoRepository.save(inventario);
    }

    private ItemEstoque getOrCreateItem(Armazem armazem, Product produto, LoteProducao lote, String unidadeMedida) {
        return itemRepository.findByProdutoAndLoteAndArmazem(produto, lote, armazem)
                .orElseGet(() -> {
                    ItemEstoque novo = new ItemEstoque();
                    novo.setArmazem(armazem);
                    novo.setProduto(produto);
                    novo.setLote(lote);
                    novo.setQuantidade(BigDecimal.ZERO);
                    novo.setReservado(BigDecimal.ZERO);
                    novo.setBloqueado(BigDecimal.ZERO);
                    novo.setUnidadeMedida(unidadeMedida);
                    aplicarMetadadosDeLote(novo, lote);
                    return novo;
                });
    }

    private ItemEstoque selecionarItemDisponivel(Product produto, Armazem armazem, LoteProducao lote, BigDecimal quantidadeNecessaria) {
        ItemEstoque item;
        if (lote != null) {
            item = itemRepository.findByProdutoAndLoteAndArmazem(produto, lote, armazem)
                    .orElseThrow(() -> new IllegalArgumentException("Lote especificado nao encontrado no armazem"));
        } else {
            List<ItemEstoque> candidatos = itemRepository.findDisponiveisFefoPeps(produto, armazem);
            if (candidatos.isEmpty()) {
                throw new IllegalArgumentException("Produto nao encontrado no estoque disponivel");
            }
            item = candidatos.stream()
                    .filter(i -> disponivel(i).compareTo(quantidadeNecessaria) >= 0)
                    .findFirst()
                    .orElse(candidatos.get(0));
        }
        validarDisponibilidade(item, quantidadeNecessaria);
        return item;
    }

    private BigDecimal disponivel(ItemEstoque item) {
        return item.getQuantidade()
                .subtract(coalesce(item.getReservado()))
                .subtract(coalesce(item.getBloqueado()));
    }

    private BigDecimal coalesce(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validarDisponibilidade(ItemEstoque item, BigDecimal quantidadeNecessaria) {
        if (disponivel(item).compareTo(quantidadeNecessaria) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente considerando reservas/bloqueios");
        }
    }

    private void validarReservadoSuficiente(ItemEstoque item, BigDecimal quantidade) {
        if (coalesce(item.getReservado()).compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Quantidade reservada insuficiente para bloqueio");
        }
    }

    private void consumirLocks(ItemEstoque item, BigDecimal saida) {
        BigDecimal restante = saida;
        BigDecimal bloqueado = coalesce(item.getBloqueado());
        if (bloqueado.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal consumir = bloqueado.min(restante);
            item.setBloqueado(bloqueado.subtract(consumir));
            restante = restante.subtract(consumir);
        }
        if (restante.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal reservado = coalesce(item.getReservado());
            BigDecimal consumir = reservado.min(restante);
            item.setReservado(reservado.subtract(consumir));
        }
    }

    private void aplicarMetadadosDeLote(ItemEstoque item, LoteProducao lote) {
        if (lote != null) {
            item.setLote(lote);
            item.setCodigoLote(lote.getCodigoLote());
            item.setDataValidade(lote.getDataValidade());
        }
    }
}
