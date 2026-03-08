package com.sustentafome.sustentafome.donation;

import com.sustentafome.sustentafome.common.Auditable;
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
@Table(indexes = {
        @Index(name = "idx_pedido_produto", columnList = "produto_id"),
        @Index(name = "idx_pedido_beneficiario", columnList = "beneficiario_id"),
        @Index(name = "idx_pedido_status", columnList = "status"),
        @Index(name = "idx_pedido_data", columnList = "data_pedido")
})
public class PedidoDoacao extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private EntidadeBeneficiaria beneficiario;

    @ManyToOne
    private CampanhaDoacao campanha;

    @ManyToOne(optional = false)
    private Product produto;

    @Column(precision = 19, scale = 4)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    private PedidoStatus status;

    private LocalDateTime dataPedido;
}
