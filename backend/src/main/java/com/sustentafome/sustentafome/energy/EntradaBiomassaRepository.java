package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface EntradaBiomassaRepository extends JpaRepository<EntradaBiomassa, Long> {
    @Query("select coalesce(sum(e.quantidadeKg),0) from EntradaBiomassa e where e.dataEntrada between :inicio and :fim")
    Double totalBiomassa(LocalDate inicio, LocalDate fim);
}
