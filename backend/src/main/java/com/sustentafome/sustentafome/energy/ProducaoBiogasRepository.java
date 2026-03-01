package com.sustentafome.sustentafome.energy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface ProducaoBiogasRepository extends JpaRepository<ProducaoBiogas, Long> {
    @Query("select coalesce(sum(p.volumeM3),0) from ProducaoBiogas p where p.data between :inicio and :fim")
    Double totalBiogas(LocalDate inicio, LocalDate fim);
}
