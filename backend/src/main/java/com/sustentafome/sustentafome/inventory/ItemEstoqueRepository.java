package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long>, JpaSpecificationExecutor<ItemEstoque> {
    Optional<ItemEstoque> findByProdutoAndLoteAndArmazem(Product produto, LoteProducao lote, Armazem armazem);

    @Query("""
            select i from ItemEstoque i
            where i.produto = :produto
              and (:armazem is null or i.armazem = :armazem)
              and (i.quantidade - coalesce(i.reservado,0) - coalesce(i.bloqueado,0)) > 0
            order by case when i.dataValidade is null then 1 else 0 end,
                     i.dataValidade,
                     i.createdAt
            """)
    List<ItemEstoque> findDisponiveisFefoPeps(@Param("produto") Product produto, @Param("armazem") Armazem armazem);

    List<ItemEstoque> findByQuantidadeLessThan(java.math.BigDecimal quantidade);

    List<ItemEstoque> findByDataValidadeIsNotNullAndDataValidadeLessThanEqual(java.time.LocalDate dataValidade);
}
