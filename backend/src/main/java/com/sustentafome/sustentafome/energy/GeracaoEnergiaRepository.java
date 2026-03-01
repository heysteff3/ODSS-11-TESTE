package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface GeracaoEnergiaRepository extends JpaRepository<GeracaoEnergia, Long> {
    @Query("select coalesce(sum(g.energiaGeradaKwh),0) from GeracaoEnergia g where g.data between :inicio and :fim")
    Double totalGerado(LocalDate inicio, LocalDate fim);
}
