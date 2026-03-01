package com.sustentafome.sustentafome.inventory.controller;

import com.sustentafome.sustentafome.inventory.dto.MovimentarEstoqueRequest;
import com.sustentafome.sustentafome.inventory.service.EstoqueMovimentacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueMovimentacaoService service;

    public EstoqueController(EstoqueMovimentacaoService service) {
        this.service = service;
    }

    @PostMapping("/movimentar")
    public ResponseEntity<?> movimentar(@RequestBody MovimentarEstoqueRequest req) {

        var resultado = service.movimentar(
                req.armazemId,
                req.produtoId,
                req.loteId,
                req.tipo,
                req.quantidade,
                req.unidadeMedida,
                req.motivo
        );

        return ResponseEntity.ok(resultado);
    }
}