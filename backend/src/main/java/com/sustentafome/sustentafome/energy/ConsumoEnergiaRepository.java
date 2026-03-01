package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface ConsumoEnergiaRepository extends JpaRepository<ConsumoEnergia, Long> {
    @Query("select coalesce(sum(c.energiaConsumidaKwh),0) from ConsumoEnergia c where c.data between :inicio and :fim")
    Double totalConsumido(LocalDate inicio, LocalDate fim);
}
