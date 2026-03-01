package com.sustentafome.sustentafome.donation;

import com.sustentafome.sustentafome.production.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PedidoDoacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private EntidadeBeneficiaria beneficiario;

    @ManyToOne
    private CampanhaDoacao campanha;

    @ManyToOne(optional = false)
    private Product produto;

    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    private PedidoStatus status;

    private LocalDateTime dataPedido;
}
