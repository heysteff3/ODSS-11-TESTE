package com.sustentafome.sustentafome.production;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteProducaoDTO(
        @NotNull Long unidadeId,
        @NotNull Long produtoId,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal quantidade,
        String unidadeMedida,
        BigDecimal custoEstimado,
        LoteStatus status,
        String observacao
) {}
