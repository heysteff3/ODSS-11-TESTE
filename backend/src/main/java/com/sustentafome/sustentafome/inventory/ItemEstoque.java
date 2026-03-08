package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.common.Auditable;
import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.Product;
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
@Table(indexes = {
        @Index(name = "idx_item_produto", columnList = "produto_id"),
        @Index(name = "idx_item_armazem", columnList = "armazem_id"),
        @Index(name = "idx_item_validade", columnList = "data_validade")
})
public class ItemEstoque extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Armazem armazem;

    @ManyToOne(optional = false)
    private Product produto;

    @ManyToOne
    private LoteProducao lote;

    /**
     * Duplicacao dos metadados de lote para acelerar consultas de FEFO/PEPS
     * e cobrir casos sem referencia a LoteProducao (entrada manual).
     */
    private String codigoLote;
    private LocalDate dataValidade;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantidade;
    private String unidadeMedida;

    @Column(precision = 19, scale = 4)
    private BigDecimal reservado = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal bloqueado = BigDecimal.ZERO;
}
