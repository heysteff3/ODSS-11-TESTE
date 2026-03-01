package com.sustentafome.sustentafome.inventory.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EstoqueMovimentacaoService {

    public record ResultadoMovimentacao(Long itemEstoqueId, BigDecimal saldoFinal) {}

    private final JdbcTemplate jdbcTemplate;

    public EstoqueMovimentacaoService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResultadoMovimentacao movimentar(
            Long armazemId,
            Long produtoId,
            Long loteId,
            String tipo,
            BigDecimal quantidade,
            String unidadeMedida,
            String motivo
    ) {
        // EXEC dbo.usp_movimentar_estoque ?,?,?,?,?,?,?
        return jdbcTemplate.queryForObject(
                "EXEC dbo.usp_movimentar_estoque ?,?,?,?,?,?,?",
                (rs, rowNum) -> new ResultadoMovimentacao(
                        rs.getLong("item_estoque_id"),
                        rs.getBigDecimal("saldo_final")
                ),
                armazemId, produtoId, loteId, tipo, quantidade, unidadeMedida, motivo
        );
    }
}