package com.sustentafome.sustentafome.inventory;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReservaRequest(
        @NotNull Long produtoId,
        Long loteId,
        @NotNull BigDecimal quantidade,
        Long pedidoId
) {}
