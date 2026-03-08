package com.sustentafome.sustentafome.donation;

import com.sustentafome.sustentafome.inventory.InventoryService;
import com.sustentafome.sustentafome.inventory.ReservaRequest;
import com.sustentafome.sustentafome.production.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final EntidadeBeneficiariaRepository beneficiariaRepository;
    private final CampanhaDoacaoRepository campanhaRepository;
    private final PedidoDoacaoRepository pedidoRepository;
    private final RotaEntregaRepository rotaRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoRepository veiculoRepository;
    private final EntregaRepository entregaRepository;
    private final EntregaEventoRepository entregaEventoRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
    private final SlaService slaService;
    private final NotificationGateway notificationGateway;

    @Cacheable("beneficiarios")
    public List<EntidadeBeneficiaria> listarBeneficiarios() {
        return beneficiariaRepository.findAll();
    }

    @CacheEvict(value = "beneficiarios", allEntries = true)
    public EntidadeBeneficiaria criarBeneficiario(EntidadeBeneficiaria e) {
        return beneficiariaRepository.save(e);
    }

    @Cacheable("campanhas")
    public List<CampanhaDoacao> listarCampanhas() {
        return campanhaRepository.findAll();
    }

    @CacheEvict(value = "campanhas", allEntries = true)
    public CampanhaDoacao criarCampanha(CampanhaDoacao c) {
        return campanhaRepository.save(c);
    }

    @Transactional
    public PedidoDoacao criarPedido(PedidoRequest request) {
        var beneficiario = beneficiariaRepository.findById(request.beneficiarioId())
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario nao encontrado"));
        var campanha = request.campanhaId() != null ? campanhaRepository.findById(request.campanhaId()).orElse(null) : null;
        var produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        PedidoDoacao pedido = new PedidoDoacao();
        pedido.setBeneficiario(beneficiario);
        pedido.setCampanha(campanha);
        pedido.setProduto(produto);
        pedido.setQuantidade(request.quantidade());
        pedido.setStatus(PedidoStatus.ABERTO);
        pedido.setDataPedido(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoDoacao reservarPedido(Long pedidoId, Long loteId) {
        PedidoDoacao pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        try {
            inventoryService.reservar(new ReservaRequest(null, pedido.getProduto().getId(), loteId, pedido.getQuantidade(), pedidoId));
        } catch (IllegalArgumentException ex) {
            slaService.alertarEstoqueCritico(pedido.getProduto().getId(), pedido.getQuantidade());
            throw ex;
        }
        pedido.setStatus(PedidoStatus.RESERVADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoDoacao avancarStatus(Long pedidoId, PedidoStatus novoStatus) {
        PedidoDoacao pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Entrega criarEntrega(EntregaRequest request) {
        PedidoDoacao pedido = pedidoRepository.findById(request.pedidoId()).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        RotaEntrega rota = request.rotaId() != null ? rotaRepository.findById(request.rotaId()).orElse(null) : null;
        Motorista mot = request.motoristaId() != null ? motoristaRepository.findById(request.motoristaId()).orElse(null) : null;
        Veiculo veiculo = request.veiculoId() != null ? veiculoRepository.findById(request.veiculoId()).orElse(null) : null;

        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setRota(rota);
        entrega.setMotorista(mot);
        entrega.setVeiculo(veiculo);
        entrega.setDataSaida(request.dataSaida());
        entrega.setDataEntrega(request.dataEntrega());
        entrega.setStatus(EntregaStatus.CRIADO);
        Entrega saved = entregaRepository.save(entrega);
        registrarEvento(saved, EntregaStatus.CRIADO, "Entrega criada");
        slaService.avaliarPedido(pedido);
        return saved;
    }

    @Transactional
    public Entrega confirmarEntrega(Long entregaId) {
        return atualizarStatusEntrega(entregaId, EntregaStatus.ENTREGUE, "Entrega confirmada");
    }

    @Transactional
    public Entrega atualizarStatusEntrega(Long entregaId, EntregaStatus status, String observacao) {
        Entrega entrega = entregaRepository.findById(entregaId).orElseThrow(() -> new IllegalArgumentException("Entrega nao encontrada"));
        entrega.setStatus(status);
        if (status == EntregaStatus.EM_ROTA && entrega.getDataSaida() == null) {
            entrega.setDataSaida(LocalDateTime.now());
        }
        if (status == EntregaStatus.ENTREGUE) {
            entrega.setDataEntrega(LocalDateTime.now());
        }
        if (entrega.getPedido() != null) {
            if (status == EntregaStatus.ENTREGUE) {
                entrega.getPedido().setStatus(PedidoStatus.ENTREGUE);
            } else if (status == EntregaStatus.SEPARADO) {
                entrega.getPedido().setStatus(PedidoStatus.SEPARADO);
            } else if (status == EntregaStatus.EM_ROTA) {
                entrega.getPedido().setStatus(PedidoStatus.EXPEDIDO);
            }
            pedidoRepository.save(entrega.getPedido());
        }
        Entrega saved = entregaRepository.save(entrega);
        registrarEvento(saved, status, observacao);
        slaService.avaliarEntrega(saved);
        return saved;
    }

    public List<EntregaEvento> listarEventos(Long entregaId) {
        return entregaEventoRepository.findByEntregaIdOrderByTimestampAsc(entregaId);
    }

    public List<DemandProjection> projetarDemanda(int diasJanela) {
        var inicio = LocalDateTime.now().minusDays(diasJanela);
        return pedidoRepository.projetarDemanda(inicio).stream()
                .map(view -> {
                    BigDecimal media = view.getMediaQuantidade() == null ? BigDecimal.ZERO : view.getMediaQuantidade();
                    return new DemandProjection(view.getBeneficiarioId(),
                            view.getCampanhaId(),
                            media,
                            media.multiply(BigDecimal.valueOf(7)));
                })
                .collect(Collectors.toList());
    }

    public Page<PedidoDoacao> listarPedidos(int page,
                                            int size,
                                            Long beneficiarioId,
                                            Long produtoId,
                                            PedidoStatus status,
                                            LocalDateTime dataDe,
                                            LocalDateTime dataAte) {
        Specification<PedidoDoacao> spec = Specification.where(null);
        if (beneficiarioId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("beneficiario").get("id"), beneficiarioId));
        }
        if (produtoId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("produto").get("id"), produtoId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (dataDe != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataPedido"), dataDe));
        }
        if (dataAte != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataPedido"), dataAte));
        }
        return pedidoRepository.findAll(spec, PageRequest.of(page, size));
    }

    public Page<Entrega> listarEntregas(int page,
                                        int size,
                                        EntregaStatus status,
                                        LocalDateTime dataSaidaDe,
                                        LocalDateTime dataSaidaAte,
                                        LocalDateTime dataEntregaDe,
                                        LocalDateTime dataEntregaAte) {
        Specification<Entrega> spec = Specification.where(null);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (dataSaidaDe != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataSaida"), dataSaidaDe));
        }
        if (dataSaidaAte != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataSaida"), dataSaidaAte));
        }
        if (dataEntregaDe != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataEntrega"), dataEntregaDe));
        }
        if (dataEntregaAte != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dataEntrega"), dataEntregaAte));
        }
        return entregaRepository.findAll(spec, PageRequest.of(page, size));
    }

    public EntregaTimelineResponse rastrearEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new IllegalArgumentException("Entrega nao encontrada"));
        var eventos = entregaEventoRepository.findByEntregaIdOrderByTimestampAsc(entregaId).stream()
                .map(ev -> new EntregaTimelineResponse.Evento(ev.getStatus(), ev.getTimestamp(), ev.getObservacao()))
                .toList();
        return new EntregaTimelineResponse(
                entrega.getId(),
                entrega.getStatus(),
                entrega.getDataSaida(),
                entrega.getDataEntrega(),
                eventos
        );
    }

    public EntregaOperacionalDashboard dashboardOperacional() {
        List<Entrega> entregas = entregaRepository.findAll();
        LocalDate hoje = LocalDate.now();

        var porStatus = entregas.stream()
                .collect(Collectors.groupingBy(Entrega::getStatus, Collectors.counting()));

        long emRota = porStatus.getOrDefault(EntregaStatus.EM_ROTA, 0L);
        long atrasadas = entregas.stream()
                .filter(e -> e.getStatus() != EntregaStatus.ENTREGUE)
                .filter(e -> e.getDataSaida() != null && e.getDataSaida().isBefore(LocalDateTime.now().minusHours(12)))
                .count();
        long entreguesHoje = entregas.stream()
                .filter(e -> e.getDataEntrega() != null && e.getDataEntrega().toLocalDate().isEqual(hoje))
                .count();

        double mediaCriacaoSaida = entregas.stream()
                .filter(e -> e.getDataSaida() != null && e.getCreatedAt() != null)
                .mapToLong(e -> Duration.between(
                        e.getCreatedAt(),
                        e.getDataSaida().atZone(ZoneId.systemDefault()).toInstant()
                ).toMinutes())
                .average().orElse(0);

        double mediaSaidaEntrega = entregas.stream()
                .filter(e -> e.getDataSaida() != null && e.getDataEntrega() != null)
                .mapToLong(e -> Duration.between(
                        e.getDataSaida(),
                        e.getDataEntrega()
                ).toMinutes())
                .average().orElse(0);

        return new EntregaOperacionalDashboard(
                porStatus,
                emRota,
                atrasadas,
                entreguesHoje,
                mediaCriacaoSaida,
                mediaSaidaEntrega
        );
    }

    private void registrarEvento(Entrega entrega, EntregaStatus status, String observacao) {
        EntregaEvento evento = new EntregaEvento();
        evento.setEntrega(entrega);
        evento.setStatus(status);
        evento.setTimestamp(LocalDateTime.now());
        evento.setObservacao(observacao);
        entregaEventoRepository.save(evento);
    }
}
