package com.sustentafome.sustentafome.production;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "produtos", key = "#page + '-' + #size")
    public Page<Product> listProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    @CacheEvict(value = "produtos", allEntries = true)
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Cacheable("unidades-produtivas")
    public List<UnidadeProdutiva> listUnidades() {
        return unidadeRepository.findAll();
    }

    @CacheEvict(value = "unidades-produtivas", allEntries = true)
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
        lote.setCodigoLote(dto.codigoLote());
        lote.setDataInicio(dto.dataInicio());
        lote.setDataFim(dto.dataFim());
        if (dto.dataValidade() != null) {
            lote.setDataValidade(dto.dataValidade());
        } else if (produto.getValidadeDiasPadrao() != null && dto.dataInicio() != null) {
            lote.setDataValidade(dto.dataInicio().plusDays(produto.getValidadeDiasPadrao()));
        }
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
