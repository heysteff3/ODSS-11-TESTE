package com.sustentafome.sustentafome.donation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record EntregaRequest(
        @NotNull Long pedidoId,
        Long rotaId,
        Long motoristaId,
        Long veiculoId,
        LocalDateTime dataSaida,
        LocalDateTime dataEntrega
) {}
