package com.sustentafome.sustentafome.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoDoacaoRepository extends JpaRepository<PedidoDoacao, Long> {
    @Query("select sum(p.quantidade) from PedidoDoacao p where p.status='ENTREGUE' and p.dataPedido between :inicio and :fim")
    Double totalDoadoPeriodo(LocalDateTime inicio, LocalDateTime fim);

    List<PedidoDoacao> findByStatus(PedidoStatus status);
}
