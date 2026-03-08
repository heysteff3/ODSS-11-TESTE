package com.sustentafome.sustentafome.production;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteProducaoDTO(
        @NotNull Long unidadeId,
        @NotNull Long produtoId,
        String codigoLote,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataValidade,
        BigDecimal quantidade,
        String unidadeMedida,
        BigDecimal custoEstimado,
        LoteStatus status,
        String observacao
) {}
