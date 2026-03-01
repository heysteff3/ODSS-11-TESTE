package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface FermentacaoRepository extends JpaRepository<Fermentacao, Long> {
    @Query("select coalesce(sum(f.co2GeradoKg),0) from Fermentacao f where f.data between :inicio and :fim")
    Double co2Gerado(LocalDate inicio, LocalDate fim);
}
