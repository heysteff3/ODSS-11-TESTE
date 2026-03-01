package com.sustentafome.sustentafome.inventory;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimentacaoRequest(
        @NotNull Long armazemId,
        @NotNull Long produtoId,
        Long loteId,
        @NotNull MovementType tipo,
        @NotNull BigDecimal quantidade,
        String unidadeMedida,
        String motivo
) {}
