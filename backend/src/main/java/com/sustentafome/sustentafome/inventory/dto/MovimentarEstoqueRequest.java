package com.sustentafome.sustentafome.inventory.dto;

import java.math.BigDecimal;

public class MovimentarEstoqueRequest {
    public Long armazemId;
    public Long produtoId;
    public Long loteId; // pode ser null
    public String tipo; // ENTRADA, SAIDA, BAIXA, RESERVA, AJUSTE_POS, AJUSTE_NEG
    public BigDecimal quantidade;
    public String unidadeMedida; // ex: "kg"
    public String motivo;
}