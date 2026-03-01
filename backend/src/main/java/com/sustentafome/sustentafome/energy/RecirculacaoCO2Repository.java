package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface RecirculacaoCO2Repository extends JpaRepository<RecirculacaoCO2, Long> {
    @Query("select coalesce(sum(r.quantidadeKg),0) from RecirculacaoCO2 r where r.data between :inicio and :fim")
    Double totalRecirculado(LocalDate inicio, LocalDate fim);
}
