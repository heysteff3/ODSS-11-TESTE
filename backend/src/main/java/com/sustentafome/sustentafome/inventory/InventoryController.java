package com.sustentafome.sustentafome.inventory;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/estoques")
public class InventoryController {
    private final InventoryService service;
    private final ArmazemRepository armazemRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final InventarioCiclicoRepository inventarioCiclicoRepository;
    private final com.sustentafome.sustentafome.donation.AlertaRepository alertaRepository;

    public InventoryController(InventoryService service,
                               ArmazemRepository armazemRepository,
                               MovimentacaoEstoqueRepository movimentacaoRepository,
                               ReservaEstoqueRepository reservaRepository,
                               InventarioCiclicoRepository inventarioCiclicoRepository,
                               com.sustentafome.sustentafome.donation.AlertaRepository alertaRepository) {
        this.service = service;
        this.armazemRepository = armazemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.reservaRepository = reservaRepository;
        this.inventarioCiclicoRepository = inventarioCiclicoRepository;
        this.alertaRepository = alertaRepository;
    }

    @GetMapping
    public Page<ItemEstoque> listEstoque(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) Long produtoId,
                                         @RequestParam(required = false) Long armazemId,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validadeDe,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validadeAte,
                                         @RequestParam(required = false, defaultValue = "false") boolean somenteDisponivel) {
        return service.listEstoque(page, size, produtoId, armazemId, validadeDe, validadeAte, somenteDisponivel);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<Armazem> createArmazem(@RequestBody @Valid Armazem armazem) {
        return ResponseEntity.ok(armazemRepository.save(armazem));
    }

    @PostMapping("/movimentacoes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<MovimentacaoEstoque> movimentar(@RequestBody @Valid MovimentacaoRequest request) {
        return ResponseEntity.ok(service.registrarMovimentacao(request));
    }

    @GetMapping("/movimentacoes")
    public Page<MovimentacaoEstoque> listMov(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return movimentacaoRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaEstoque> reservar(@RequestBody @Valid ReservaRequest request) {
        return ResponseEntity.ok(service.reservar(request));
    }

    @GetMapping("/reservas")
    public Page<ReservaEstoque> listReservas(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return reservaRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }

    @PostMapping("/reservas/{id}/bloquear")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<ReservaEstoque> bloquearReserva(@PathVariable Long id) {
        return ResponseEntity.ok(service.bloquearReserva(id));
    }

    @PostMapping("/transferencias")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<TransferenciaResponse> transferir(@RequestBody @Valid TransferenciaRequest request) {
        return ResponseEntity.ok(service.transferir(request));
    }

    @PostMapping("/contagens")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<InventarioCiclico> registrarContagem(@RequestBody @Valid ContagemCiclicaRequest request) {
        return ResponseEntity.ok(service.registrarContagemCiclica(request));
    }

    @GetMapping("/contagens")
    public Page<InventarioCiclico> listarContagens(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return inventarioCiclicoRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
    }

    @Operation(summary = "Listar alertas de estoque", description = "Retorna alertas ativos ou históricos de estoque crítico e validade")
    @GetMapping("/alertas")
    public ResponseEntity<?> listarAlertas() {
        return ResponseEntity.ok(alertaRepository.findAll());
    }
}
