package com.sustentafome.sustentafome.production;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final UnidadeProdutivaRepository unidadeRepository;
    private final ProductRepository productRepository;
    private final LoteProducaoRepository loteRepository;

    public Page<Product> listProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<UnidadeProdutiva> listUnidades() {
        return unidadeRepository.findAll();
    }

    public UnidadeProdutiva createUnidade(UnidadeProdutiva unidade) {
        return unidadeRepository.save(unidade);
    }

    @Transactional
    public LoteProducao createLote(LoteProducaoDTO dto) {
        var unidade = unidadeRepository.findById(dto.unidadeId()).orElseThrow(() -> new IllegalArgumentException("Unidade nao encontrada"));
        var produto = productRepository.findById(dto.produtoId()).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));

        LoteProducao lote = new LoteProducao();
        lote.setUnidade(unidade);
        lote.setProduto(produto);
        lote.setDataInicio(dto.dataInicio());
        lote.setDataFim(dto.dataFim());
        lote.setQuantidade(dto.quantidade());
        lote.setUnidadeMedida(dto.unidadeMedida());
        lote.setCustoEstimado(dto.custoEstimado());
        lote.setStatus(dto.status() == null ? LoteStatus.PLANEJADO : dto.status());
        lote.setObservacao(dto.observacao());
        return loteRepository.save(lote);
    }

    public Page<LoteProducao> listLotes(int page, int size) {
        return loteRepository.findAll(PageRequest.of(page, size));
    }
}
