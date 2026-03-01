package com.sustentafome.sustentafome.inventory;

import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {
    Optional<ItemEstoque> findByProdutoAndLoteAndArmazem(Product produto, LoteProducao lote, Armazem armazem);
}
