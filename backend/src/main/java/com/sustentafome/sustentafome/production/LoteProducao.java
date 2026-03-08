package com.sustentafome.sustentafome.production;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LoteProducao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Codigo externo/etiqueta do lote (visivel na expedicao e inventario).
     */
    @Column(length = 64)
    private String codigoLote;

    @ManyToOne(optional = false)
    private UnidadeProdutiva unidade;

    @ManyToOne(optional = false)
    private Product produto;

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private LocalDate dataValidade;
    private BigDecimal quantidade;
    private String unidadeMedida;
    private BigDecimal custoEstimado;

    @Enumerated(EnumType.STRING)
    private LoteStatus status;

    private String observacao;
}
