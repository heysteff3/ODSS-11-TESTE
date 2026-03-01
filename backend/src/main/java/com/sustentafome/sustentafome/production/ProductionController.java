package com.sustentafome.sustentafome.production;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductionController {
    private final ProductionService service;

    public ProductionController(ProductionService service) {
        this.service = service;
    }

    @GetMapping("/produtos")
    public Page<Product> listProdutos(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return service.listProducts(page, size);
    }

    @PostMapping("/produtos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<Product> createProduto(@RequestBody @Valid Product product) {
        return ResponseEntity.ok(service.createProduct(product));
    }

    @GetMapping("/unidades-produtivas")
    public ResponseEntity<?> listUnidades() {
        return ResponseEntity.ok(service.listUnidades());
    }

    @PostMapping("/unidades-produtivas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<UnidadeProdutiva> createUnidade(@RequestBody @Valid UnidadeProdutiva unidade) {
        return ResponseEntity.ok(service.createUnidade(unidade));
    }

    @GetMapping("/lotes")
    public Page<LoteProducao> listLotes(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return service.listLotes(page, size);
    }

    @PostMapping("/lotes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERADOR')")
    public ResponseEntity<LoteProducao> createLote(@RequestBody @Valid LoteProducaoDTO dto) {
        return ResponseEntity.ok(service.createLote(dto));
    }
}
