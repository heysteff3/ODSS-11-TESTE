package com.sustentafome.sustentafome.energy;

import com.sustentafome.sustentafome.donation.PedidoDoacaoRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class EnergyService {
    private final BiodigestorRepository biodigestorRepository;
    private final EntradaBiomassaRepository entradaBiomassaRepository;
    private final ProducaoBiogasRepository producaoBiogasRepository;
    private final TermoeletricaRepository termoeletricaRepository;
    private final GeracaoEnergiaRepository geracaoEnergiaRepository;
    private final ConsumoEnergiaRepository consumoEnergiaRepository;
    private final FermentacaoRepository fermentacaoRepository;
    private final DestilacaoRepository destilacaoRepository;
    private final EmissaoCO2Repository emissaoCO2Repository;
    private final RecirculacaoCO2Repository recirculacaoCO2Repository;
    private final PedidoDoacaoRepository pedidoDoacaoRepository;
    private final RestTemplate restTemplate;
    private final Counter leiturasImportadasCounter;

    public EnergyService(BiodigestorRepository biodigestorRepository,
                         EntradaBiomassaRepository entradaBiomassaRepository,
                         ProducaoBiogasRepository producaoBiogasRepository,
                         TermoeletricaRepository termoeletricaRepository,
                         GeracaoEnergiaRepository geracaoEnergiaRepository,
                         ConsumoEnergiaRepository consumoEnergiaRepository,
                         FermentacaoRepository fermentacaoRepository,
                         DestilacaoRepository destilacaoRepository,
                         EmissaoCO2Repository emissaoCO2Repository,
                         RecirculacaoCO2Repository recirculacaoCO2Repository,
                         PedidoDoacaoRepository pedidoDoacaoRepository,
                         RestTemplate restTemplate,
                         MeterRegistry meterRegistry) {
        this.biodigestorRepository = biodigestorRepository;
        this.entradaBiomassaRepository = entradaBiomassaRepository;
        this.producaoBiogasRepository = producaoBiogasRepository;
        this.termoeletricaRepository = termoeletricaRepository;
        this.geracaoEnergiaRepository = geracaoEnergiaRepository;
        this.consumoEnergiaRepository = consumoEnergiaRepository;
        this.fermentacaoRepository = fermentacaoRepository;
        this.destilacaoRepository = destilacaoRepository;
        this.emissaoCO2Repository = emissaoCO2Repository;
        this.recirculacaoCO2Repository = recirculacaoCO2Repository;
        this.pedidoDoacaoRepository = pedidoDoacaoRepository;
        this.restTemplate = restTemplate;
        this.leiturasImportadasCounter = Counter.builder("sustentafome.energia.leituras_importadas")
                .description("Quantidade de leituras de energia importadas via CSV/API")
                .register(meterRegistry);
    }

    public Page<EntradaBiomassa> listEntradas(int page, int size) {
        return entradaBiomassaRepository.findAll(PageRequest.of(page, size));
    }

    public EntradaBiomassa registrarEntrada(EntradaBiomassa entrada) {
        return entradaBiomassaRepository.save(entrada);
    }

    public ProducaoBiogas registrarBiogas(ProducaoBiogas biogas) {return producaoBiogasRepository.save(biogas);}    

    public GeracaoEnergia registrarGeracao(GeracaoEnergia geracao) {return geracaoEnergiaRepository.save(geracao);}    

    public ConsumoEnergia registrarConsumo(ConsumoEnergia consumo) {return consumoEnergiaRepository.save(consumo);}    

    public Fermentacao registrarFermentacao(Fermentacao f) {return fermentacaoRepository.save(f);}    

    public Destilacao registrarDestilacao(Destilacao d) {return destilacaoRepository.save(d);}    

    public EmissaoCO2 registrarEmissao(EmissaoCO2 e) {return emissaoCO2Repository.save(e);}    

    public RecirculacaoCO2 registrarRecirculacao(RecirculacaoCO2 r) {return recirculacaoCO2Repository.save(r);}    

    @Transactional(readOnly = true)
    public SimulacaoResponse simular(LocalDate inicio, LocalDate fim) {
        Double gerado = geracaoEnergiaRepository.totalGerado(inicio, fim);
        Double consumido = consumoEnergiaRepository.totalConsumido(inicio, fim);
        Double saldo = gerado - consumido;
        Double biogas = producaoBiogasRepository.totalBiogas(inicio, fim);
        Double biomassa = entradaBiomassaRepository.totalBiomassa(inicio, fim);
        Double co2Recirculado = recirculacaoCO2Repository.totalRecirculado(inicio, fim);
        Double alimentoDoado = Optional.ofNullable(pedidoDoacaoRepository.totalDoadoPeriodo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())).orElse(0d);
        Double kgPorKwh = gerado > 0 ? alimentoDoado / gerado : 0d;
        return new SimulacaoResponse(gerado, consumido, saldo, kgPorKwh, co2Recirculado, biomassa, biogas);
    }

    @Transactional(readOnly = true)
    public SimulacaoResponse kpis(LocalDate inicio, LocalDate fim) {
        // reutiliza SimulacaoResponse como pacote de KPIs-chave
        return simular(inicio, fim);
    }

    @Transactional
    public int importarCsv(ImportLeituraCsvRequest request) {
        if (request == null || request.csv() == null || request.tipo() == null) return 0;
        String[] linhas = request.csv().split("\\r?\\n");
        int imported = 0;
        for (String linha : linhas) {
            if (linha.isBlank()) continue;
            String clean = linha.trim();
            if (clean.toLowerCase().startsWith("data")) continue;
            String[] parts = clean.split(",");
            if (parts.length < 2) continue;
            LocalDate data = LocalDate.parse(parts[0].trim());
            Double valor = Double.parseDouble(parts[1].trim());
            persistLeitura(request.tipo(), data, valor);
            imported++;
        }
        if (imported > 0) leiturasImportadasCounter.increment(imported);
        return imported;
    }

    @Transactional
    public int importarApi(ImportLeituraApiRequest request) {
        if (request == null || request.url() == null || request.tipo() == null) return 0;
        var resposta = restTemplate.getForObject(request.url(), LeituraExterna[].class);
        if (resposta == null) return 0;
        int imported = 0;
        for (LeituraExterna l : resposta) {
            if (l == null || l.data == null || l.valor == null) continue;
            persistLeitura(request.tipo(), l.data, l.valor);
            imported++;
        }
        if (imported > 0) leiturasImportadasCounter.increment(imported);
        return imported;
    }

    private void persistLeitura(LeituraTipo tipo, LocalDate data, Double valor) {
        switch (tipo) {
            case GERACAO -> geracaoEnergiaRepository.save(new GeracaoEnergia(null, null, data, valor));
            case CONSUMO -> consumoEnergiaRepository.save(new ConsumoEnergia(null, "API/CSV", data, valor));
        }
    }

    public record LeituraExterna(LocalDate data, Double valor) {}
}
