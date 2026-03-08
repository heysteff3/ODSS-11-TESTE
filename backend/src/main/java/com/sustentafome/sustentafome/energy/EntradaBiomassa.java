package com.sustentafome.sustentafome.energy;

import com.sustentafome.sustentafome.common.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EntradaBiomassa extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Biodigestor biodigestor;
    private String tipoBiomassa;
    private Double quantidadeKg;
    private LocalDate dataEntrada;
}
