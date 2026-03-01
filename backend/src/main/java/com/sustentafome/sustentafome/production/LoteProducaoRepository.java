package com.sustentafome.sustentafome.production;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LoteProducaoRepository extends JpaRepository<LoteProducao, Long> {
    @Query("select l from LoteProducao l where l.dataInicio>=:inicio and l.dataFim<=:fim")
    List<LoteProducao> findByPeriodo(LocalDate inicio, LocalDate fim);
}
