package com.sustentafome.sustentafome.energy;

public record SimulacaoResponse(Double energiaGeradaKwh,
                                Double energiaConsumidaKwh,
                                Double saldoEnergeticoKwh,
                                Double kgAlimentoPorKwh,
                                Double co2RecirculadoKg,
                                Double biomassaProcessadaKg,
                                Double biogasProduzidoM3) {}
