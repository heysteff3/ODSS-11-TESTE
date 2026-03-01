package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.LoteProducaoRepository;
import com.sustentafome.sustentafome.production.Product;
import com.sustentafome.sustentafome.production.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ArmazemRepository armazemRepository;
    private final ItemEstoqueRepository itemRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final ProductRepository productRepository;
    private final LoteProducaoRepository loteRepository;

    public Page<ItemEstoque> listEstoque(int page, int size) {
        return itemRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional
    public MovimentacaoEstoque registrarMovimentacao(MovimentacaoRequest request) {
        var armazem = armazemRepository.findById(request.armazemId())
                .orElseThrow(() -> new IllegalArgumentException("Armazem nao encontrado"));
        Product produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        LoteProducao lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;

        ItemEstoque item = itemRepository.findByProdutoAndLoteAndArmazem(produto, lote, armazem)
                .orElseGet(() -> {
                    ItemEstoque novo = new ItemEstoque();
                    novo.setArmazem(armazem);
                    novo.setProduto(produto);
                    novo.setLote(lote);
                    novo.setQuantidade(BigDecimal.ZERO);
                    novo.setUnidadeMedida(request.unidadeMedida());
                    return novo;
                });

        BigDecimal quantidade = request.quantidade();
        if (request.tipo() == MovementType.SAIDA && item.getQuantidade().compareTo(quantidade) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para saida");
        }
        if (request.tipo() == MovementType.SAIDA) {
            item.setQuantidade(item.getQuantidade().subtract(quantidade));
        } else {
            item.setQuantidade(item.getQuantidade().add(quantidade));
        }
        itemRepository.save(item);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setArmazem(armazem);
        mov.setProduto(produto);
        mov.setLote(lote);
        mov.setTipo(request.tipo());
        mov.setQuantidade(quantidade);
        mov.setMotivo(request.motivo());
        mov.setDataMovimentacao(LocalDateTime.now());
        return movimentacaoRepository.save(mov);
    }

    @Transactional
    public ReservaEstoque reservar(ReservaRequest request) {
        Product produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        LoteProducao lote = request.loteId() != null ? loteRepository.findById(request.loteId()).orElse(null) : null;
        ItemEstoque item = itemRepository.findAll().stream()
                .filter(i -> i.getProduto().equals(produto) && (lote == null || lote.equals(i.getLote())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado no estoque"));
        if (item.getQuantidade().compareTo(request.quantidade()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para reserva");
        }
        item.setQuantidade(item.getQuantidade().subtract(request.quantidade()));
        itemRepository.save(item);

        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProduto(produto);
        reserva.setLote(lote);
        reserva.setQuantidade(request.quantidade());
        reserva.setStatus(ReservaStatus.ATIVA);
        reserva.setDataReserva(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }
}
