package com.sustentafome.sustentafome.donation;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class DonationController {
    private final DonationService service;
    private final EntidadeBeneficiariaRepository beneficiariaRepository;
    private final CampanhaDoacaoRepository campanhaRepository;
    private final PedidoDoacaoRepository pedidoRepository;
    private final EntregaRepository entregaRepository;
    private final RotaEntregaRepository rotaRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoRepository veiculoRepository;

    public DonationController(DonationService service, EntidadeBeneficiariaRepository beneficiariaRepository, CampanhaDoacaoRepository campanhaRepository, PedidoDoacaoRepository pedidoRepository, EntregaRepository entregaRepository, RotaEntregaRepository rotaRepository, MotoristaRepository motoristaRepository, VeiculoRepository veiculoRepository) {
        this.service = service;
        this.beneficiariaRepository = beneficiariaRepository;
        this.campanhaRepository = campanhaRepository;
        this.pedidoRepository = pedidoRepository;
        this.entregaRepository = entregaRepository;
        this.rotaRepository = rotaRepository;
        this.motoristaRepository = motoristaRepository;
        this.veiculoRepository = veiculoRepository;
    }

    @GetMapping("/beneficiarios")
    public ResponseEntity<?> listBeneficiarios() {return ResponseEntity.ok(beneficiariaRepository.findAll());}

    @PostMapping("/beneficiarios")
    public ResponseEntity<EntidadeBeneficiaria> createBeneficiario(@RequestBody @Valid EntidadeBeneficiaria e) {
        return ResponseEntity.ok(beneficiariaRepository.save(e));
    }

    @GetMapping("/campanhas")
    public ResponseEntity<?> listCampanhas() {return ResponseEntity.ok(campanhaRepository.findAll());}

    @PostMapping("/campanhas")
    public ResponseEntity<CampanhaDoacao> createCampanha(@RequestBody @Valid CampanhaDoacao c) {
        return ResponseEntity.ok(campanhaRepository.save(c));
    }

    @GetMapping("/pedidos-doacao")
    public Page<PedidoDoacao> listPedidos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return pedidoRepository.findAll(PageRequest.of(page, size));
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

    @GetMapping("/entregas")
    public Page<Entrega> listEntregas(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return entregaRepository.findAll(PageRequest.of(page, size));
    }

    @PostMapping("/rotas")
    public ResponseEntity<RotaEntrega> createRota(@RequestBody @Valid RotaEntrega rota) {return ResponseEntity.ok(rotaRepository.save(rota));}

    @GetMapping("/rotas")
    public ResponseEntity<?> listRotas() {return ResponseEntity.ok(rotaRepository.findAll());}

    @PostMapping("/motoristas")
    public ResponseEntity<Motorista> createMotorista(@RequestBody @Valid Motorista m) {return ResponseEntity.ok(motoristaRepository.save(m));}

    @PostMapping("/veiculos")
    public ResponseEntity<Veiculo> createVeiculo(@RequestBody @Valid Veiculo v) {return ResponseEntity.ok(veiculoRepository.save(v));}
}
