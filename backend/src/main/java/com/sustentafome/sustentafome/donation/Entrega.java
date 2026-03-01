package com.sustentafome.sustentafome.donation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Entrega {
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
