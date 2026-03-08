package com.sustentafome.sustentafome.donation;

import com.sustentafome.sustentafome.common.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_entrega_status", columnList = "status"),
        @Index(name = "idx_entrega_saida", columnList = "data_saida"),
        @Index(name = "idx_entrega_entrega", columnList = "data_entrega")
})
public class Entrega extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PedidoDoacao pedido;

    @ManyToOne
    private RotaEntrega rota;

    @ManyToOne
    private Motorista motorista;

    @ManyToOne
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    private EntregaStatus status;

    private LocalDateTime dataSaida;
    private LocalDateTime dataEntrega;
}
