package com.sustentafome.sustentafome.donation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoDoacaoRepository extends JpaRepository<PedidoDoacao, Long>, JpaSpecificationExecutor<PedidoDoacao> {
    @Query("select sum(p.quantidade) from PedidoDoacao p where p.status='ENTREGUE' and p.dataPedido between :inicio and :fim")
    Double totalDoadoPeriodo(LocalDateTime inicio, LocalDateTime fim);

    List<PedidoDoacao> findByStatus(PedidoStatus status);

    @Query("select p.beneficiario.id as beneficiarioId, p.campanha.id as campanhaId, avg(p.quantidade) as mediaQuantidade " +
            "from PedidoDoacao p where p.dataPedido >= :inicio group by p.beneficiario.id, p.campanha.id")
    List<DemandProjectionView> projetarDemanda(@Param("inicio") LocalDateTime inicio);
}
