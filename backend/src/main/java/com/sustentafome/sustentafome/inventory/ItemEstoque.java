package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ItemEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Armazem armazem;

    @ManyToOne(optional = false)
    private Product produto;

    @ManyToOne
    private LoteProducao lote;

    private BigDecimal quantidade;
    private String unidadeMedida;
}
