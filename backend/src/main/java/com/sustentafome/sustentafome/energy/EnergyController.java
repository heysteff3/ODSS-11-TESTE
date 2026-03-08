package com.sustentafome.sustentafome.energy;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/energia")
public class EnergyController {
    private final EnergyService service;

    public EnergyController(EnergyService service) {
        this.service = service;
    }

    @PostMapping("/biodigestor/entradas")
    public ResponseEntity<EntradaBiomassa> registrarEntrada(@RequestBody EntradaBiomassa entrada) {
        return ResponseEntity.ok(service.registrarEntrada(entrada));
    }

    @GetMapping("/biodigestor/entradas")
    public ResponseEntity<?> listarEntradas(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.listEntradas(page, size));
    }

    @PostMapping("/biogas")
    public ResponseEntity<ProducaoBiogas> biogas(@RequestBody ProducaoBiogas p) {return ResponseEntity.ok(service.registrarBiogas(p));}

    @PostMapping("/geracao")
    public ResponseEntity<GeracaoEnergia> geracao(@RequestBody GeracaoEnergia g) {return ResponseEntity.ok(service.registrarGeracao(g));}

    @PostMapping("/consumo")
    public ResponseEntity<ConsumoEnergia> consumo(@RequestBody ConsumoEnergia c) {return ResponseEntity.ok(service.registrarConsumo(c));}

    @PostMapping("/fermentacao")
    public ResponseEntity<Fermentacao> fermentacao(@RequestBody Fermentacao f) {return ResponseEntity.ok(service.registrarFermentacao(f));}

    @PostMapping("/destilacao")
    public ResponseEntity<Destilacao> destilacao(@RequestBody Destilacao d) {return ResponseEntity.ok(service.registrarDestilacao(d));}

    @PostMapping("/emissao-co2")
    public ResponseEntity<EmissaoCO2> emissao(@RequestBody EmissaoCO2 e) {return ResponseEntity.ok(service.registrarEmissao(e));}

    @PostMapping("/recirculacao-co2")
    public ResponseEntity<RecirculacaoCO2> recirculacao(@RequestBody RecirculacaoCO2 r) {return ResponseEntity.ok(service.registrarRecirculacao(r));}

    @GetMapping("/simulacao")
    public ResponseEntity<SimulacaoResponse> simular(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.simular(inicio, fim));
    }

    @Operation(summary = "KPIs consolidados de energia", description = "Retorna geração, consumo, saldo, biogás, biomassa, CO2 e kg por kWh no período")
    @GetMapping("/kpis")
    public ResponseEntity<SimulacaoResponse> kpis(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.kpis(inicio, fim));
    }

    @Operation(summary = "Importar leituras de energia via CSV", description = "CSV com colunas data,valor; tipo pode ser GERACAO ou CONSUMO")
    @PostMapping("/leituras/import-csv")
    public ResponseEntity<Integer> importarCsv(@RequestBody ImportLeituraCsvRequest request) {
        return ResponseEntity.ok(service.importarCsv(request));
    }

    @Operation(summary = "Importar leituras de energia via API externa", description = "URL deve retornar lista de objetos {data, valor}; tipo pode ser GERACAO ou CONSUMO")
    @PostMapping("/leituras/import-api")
    public ResponseEntity<Integer> importarApi(@RequestBody ImportLeituraApiRequest request) {
        return ResponseEntity.ok(service.importarApi(request));
    }
}
