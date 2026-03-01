package com.sustentafome.sustentafome.energy;

import com.sustentafome.sustentafome.donation.PedidoDoacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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
}
