package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.common.Auditable;
import com.sustentafome.sustentafome.donation.PedidoDoacao;
import com.sustentafome.sustentafome.production.LoteProducao;
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
public class ReservaEstoque extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Armazem armazem;

    @ManyToOne(optional = false)
    private Product produto;

    @ManyToOne
    private LoteProducao lote;

    @Enumerated(EnumType.STRING)
    private ReservaStatus status;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantidade;
    private LocalDateTime dataReserva;

    @ManyToOne
    private PedidoDoacao pedido;
}
