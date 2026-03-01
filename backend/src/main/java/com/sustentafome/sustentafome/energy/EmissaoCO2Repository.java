package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface EmissaoCO2Repository extends JpaRepository<EmissaoCO2, Long> {
    @Query("select coalesce(sum(e.quantidadeKg),0) from EmissaoCO2 e where e.data between :inicio and :fim")
    Double totalEmissao(LocalDate inicio, LocalDate fim);
}
