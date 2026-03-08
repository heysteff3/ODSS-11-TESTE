package com.sustentafome.sustentafome.dashboard;

import com.sustentafome.sustentafome.donation.Alerta;
import com.sustentafome.sustentafome.donation.AlertaRepository;
import com.sustentafome.sustentafome.donation.AlertaTipo;
import com.sustentafome.sustentafome.donation.PedidoDoacaoRepository;
import com.sustentafome.sustentafome.donation.PedidoStatus;
import com.sustentafome.sustentafome.energy.*;
import com.sustentafome.sustentafome.inventory.ItemEstoque;
import com.sustentafome.sustentafome.inventory.ItemEstoqueRepository;
import com.sustentafome.sustentafome.production.LoteProducao;
import com.sustentafome.sustentafome.production.LoteProducaoRepository;
import com.sustentafome.sustentafome.production.UnidadeProdutivaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final LoteProducaoRepository loteRepository;
    private final UnidadeProdutivaRepository unidadeRepository;
    private final ItemEstoqueRepository itemEstoqueRepository;
    private final PedidoDoacaoRepository pedidoRepository;
    private final GeracaoEnergiaRepository geracaoEnergiaRepository;
    private final ConsumoEnergiaRepository consumoEnergiaRepository;
    private final ProducaoBiogasRepository biogasRepository;
    private final RecirculacaoCO2Repository recirculacaoCO2Repository;
    private final AlertaRepository alertaRepository;

    public Map<String, Object> producao(LocalDate inicio, LocalDate fim) {
        List<LoteProducao> lotes = loteRepository.findByPeriodo(inicio, fim);
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalLotes", lotes.size());
        resp.put("porUnidade", lotes.stream().collect(Collectors.groupingBy(l -> l.getUnidade().getNome(), Collectors.counting())));
        resp.put("porProduto", lotes.stream().collect(Collectors.groupingBy(l -> l.getProduto().getNome(), Collectors.summingDouble(l -> l.getQuantidade() != null ? l.getQuantidade().doubleValue() : 0))));
        return resp;
    }

    public Map<String, Object> estoque() {
        Map<String, Object> resp = new HashMap<>();
        List<ItemEstoque> itens = itemEstoqueRepository.findAll();
        resp.put("saldo", itens.stream().collect(Collectors.groupingBy(i -> i.getProduto().getNome(), Collectors.summingDouble(i -> i.getQuantidade() != null ? i.getQuantidade().doubleValue() : 0))));
        resp.put("porArmazem", itens.stream().collect(Collectors.groupingBy(i -> i.getArmazem().getNome(), Collectors.counting())));
        return resp;
    }

    public Map<String, Object> doacoes(LocalDate inicio, LocalDate fim) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalEntregue", pedidoRepository.totalDoadoPeriodo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()));
        resp.put("porStatus", pedidoRepository.findAll().stream().collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting())));
        return resp;
    }

    public Map<String, Object> energia(LocalDate inicio, LocalDate fim) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("geradoKwh", geracaoEnergiaRepository.totalGerado(inicio, fim));
        resp.put("consumidoKwh", consumoEnergiaRepository.totalConsumido(inicio, fim));
        resp.put("biogasM3", biogasRepository.totalBiogas(inicio, fim));
        resp.put("co2RecirculadoKg", recirculacaoCO2Repository.totalRecirculado(inicio, fim));
        return resp;
    }

    public Map<String, Object> alertas() {
        Map<String, Object> resp = new HashMap<>();
        var lowStock = alertaRepository.findAll().stream()
                .filter(a -> !a.isResolvido() && a.getTipo() == AlertaTipo.ESTOQUE_CRITICO)
                .map(Alerta::getMensagem)
                .toList();
        var validade = alertaRepository.findAll().stream()
                .filter(a -> !a.isResolvido() && a.getTipo() == AlertaTipo.EXPIRACAO_ESTOQUE)
                .map(Alerta::getMensagem)
                .toList();
        var pedidosAtrasados = pedidoRepository.findByStatus(PedidoStatus.RESERVADO).stream().map(p -> p.getId()).toList();
        var unidadesParadas = unidadeRepository.findAll().stream().filter(u -> !u.isAtiva()).map(u -> u.getNome()).toList();
        resp.put("estoque", lowStock);
        resp.put("validade", validade);
        resp.put("pedidosAtrasados", pedidosAtrasados);
        resp.put("unidadesParadas", unidadesParadas);
        return resp;
    }
}
