package com.sustentafome.sustentafome.donation;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PedidoRequest(
        @NotNull Long beneficiarioId,
        Long campanhaId,
        @NotNull Long produtoId,
        @NotNull BigDecimal quantidade
) {}
