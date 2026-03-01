package com.sustentafome.sustentafome.inventory;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

    public InventoryController(InventoryService service, ArmazemRepository armazemRepository, MovimentacaoEstoqueRepository movimentacaoRepository, ReservaEstoqueRepository reservaRepository) {
        this.service = service;
        this.armazemRepository = armazemRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping
    public Page<ItemEstoque> listEstoque(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return service.listEstoque(page, size);
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
}
