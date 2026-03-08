package com.sustentafome.sustentafome.donation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DonationController {
    private final DonationService service;
    private final RouteOptimizationService routeOptimizationService;
    private final EntidadeBeneficiariaRepository beneficiariaRepository;
    private final CampanhaDoacaoRepository campanhaRepository;
    private final PedidoDoacaoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final RotaEntregaRepository rotaRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoRepository veiculoRepository;
    private final AlertaRepository alertaRepository;

    public DonationController(DonationService service, RouteOptimizationService routeOptimizationService, EntidadeBeneficiariaRepository beneficiariaRepository, CampanhaDoacaoRepository campanhaRepository, PedidoDoacaoRepository pedidoRepository, EntregaRepository entregaRepository, RotaEntregaRepository rotaRepository, MotoristaRepository motoristaRepository, VeiculoRepository veiculoRepository, AlertaRepository alertaRepository) {
        this.service = service;
        this.routeOptimizationService = routeOptimizationService;
        this.beneficiariaRepository = beneficiariaRepository;
        this.campanhaRepository = campanhaRepository;
        this.pedidoRepository = pedidoRepository;
        this.entregaRepository = entregaRepository;
        this.rotaRepository = rotaRepository;
        this.motoristaRepository = motoristaRepository;
        this.veiculoRepository = veiculoRepository;
        this.alertaRepository = alertaRepository;
    }

    @GetMapping("/beneficiarios")
    public ResponseEntity<?> listBeneficiarios() {return ResponseEntity.ok(service.listarBeneficiarios());}

    @PostMapping("/beneficiarios")
    public ResponseEntity<EntidadeBeneficiaria> createBeneficiario(@RequestBody @Valid EntidadeBeneficiaria e) {
        return ResponseEntity.ok(service.criarBeneficiario(e));
    }

    @GetMapping("/campanhas")
    public ResponseEntity<?> listCampanhas() {return ResponseEntity.ok(service.listarCampanhas());}

    @PostMapping("/campanhas")
    public ResponseEntity<CampanhaDoacao> createCampanha(@RequestBody @Valid CampanhaDoacao c) {
        return ResponseEntity.ok(service.criarCampanha(c));
    }

    @GetMapping("/pedidos-doacao")
    public Page<PedidoDoacao> listPedidos(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) Long beneficiarioId,
                                          @RequestParam(required = false) Long produtoId,
                                          @RequestParam(required = false) PedidoStatus status,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataDe,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataAte) {
        return service.listarPedidos(page, size, beneficiarioId, produtoId, status, dataDe, dataAte);
    }

    @PostMapping("/pedidos-doacao")
    public ResponseEntity<PedidoDoacao> criarPedido(@RequestBody @Valid PedidoRequest request) {
        return ResponseEntity.ok(service.criarPedido(request));
    }

    @PostMapping("/pedidos-doacao/{id}/reservar")
    public ResponseEntity<PedidoDoacao> reservar(@PathVariable Long id, @RequestParam(required = false) Long loteId) {
        return ResponseEntity.ok(service.reservarPedido(id, loteId));
    }

    @PostMapping("/pedidos-doacao/{id}/status/{status}")
    public ResponseEntity<PedidoDoacao> avancar(@PathVariable Long id, @PathVariable PedidoStatus status) {
        return ResponseEntity.ok(service.avancarStatus(id, status));
    }

    @PostMapping("/entregas")
    public ResponseEntity<Entrega> criarEntrega(@RequestBody @Valid EntregaRequest request) {
        return ResponseEntity.ok(service.criarEntrega(request));
    }

    @PostMapping("/entregas/{id}/confirmar")
    public ResponseEntity<Entrega> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmarEntrega(id));
    }

    @PostMapping("/entregas/{id}/status")
    public ResponseEntity<Entrega> atualizarStatus(@PathVariable Long id, @RequestBody @Valid UpdateEntregaStatusRequest request) {
        return ResponseEntity.ok(service.atualizarStatusEntrega(id, request.status(), request.observacao()));
    }

    @GetMapping("/entregas/{id}/eventos")
    public ResponseEntity<?> eventos(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarEventos(id));
    }

    @Operation(summary = "Rastreio detalhado de uma entrega", description = "Retorna timeline de status e timestamps da entrega")
    @GetMapping("/entregas/{id}/rastreio")
    public ResponseEntity<EntregaTimelineResponse> rastrear(@PathVariable Long id) {
        return ResponseEntity.ok(service.rastrearEntrega(id));
    }

    @Operation(summary = "Dashboard operacional de entregas", description = "Resumo por status, atrasos e tempos médios de ciclo")
    @GetMapping("/entregas/operacional")
    public ResponseEntity<EntregaOperacionalDashboard> dashboardOperacional() {
        return ResponseEntity.ok(service.dashboardOperacional());
    }

    @GetMapping("/entregas")
    public Page<Entrega> listEntregas(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) EntregaStatus status,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saidaDe,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime saidaAte,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime entregaDe,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime entregaAte) {
        return service.listarEntregas(page, size, status, saidaDe, saidaAte, entregaDe, entregaAte);
    }

    @PostMapping("/rotas")
    public ResponseEntity<RotaEntrega> createRota(@RequestBody @Valid RotaEntrega rota) {return ResponseEntity.ok(rotaRepository.save(rota));}

    @GetMapping("/rotas")
    public ResponseEntity<?> listRotas() {return ResponseEntity.ok(rotaRepository.findAll());}

    @PostMapping("/motoristas")
    public ResponseEntity<Motorista> createMotorista(@RequestBody @Valid Motorista m) {return ResponseEntity.ok(motoristaRepository.save(m));}

    @PostMapping("/veiculos")
    public ResponseEntity<Veiculo> createVeiculo(@RequestBody @Valid Veiculo v) {return ResponseEntity.ok(veiculoRepository.save(v));}

    @PostMapping("/rotas/otimizar")
    public ResponseEntity<List<RotaSugestao>> otimizarRotas(@RequestBody @Valid OptimizeRotaRequest request) {
        return ResponseEntity.ok(routeOptimizationService.otimizar(request));
    }

    @GetMapping("/alertas")
    public ResponseEntity<?> listarAlertas() {return ResponseEntity.ok(alertaRepository.findAll());}

    @GetMapping("/pedidos-doacao/demanda/projecao")
    public ResponseEntity<List<DemandProjection>> projetarDemanda(@RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(service.projetarDemanda(dias));
    }
}
