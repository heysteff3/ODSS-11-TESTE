package com.sustentafome.sustentafome.production;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String categoria;

    /**
     * Indica se o produto requer rastreabilidade por lote/validade.
     * Controla regras de FEFO/PEPS na expedicao.
     */
    private Boolean perecivel = Boolean.FALSE;

    /**
     * Validade padrao em dias para novos lotes (opcional).
     * Quando informado, a data de validade pode ser derivada a partir da data de inicio do lote.
     */
    private Integer validadeDiasPadrao;
}
