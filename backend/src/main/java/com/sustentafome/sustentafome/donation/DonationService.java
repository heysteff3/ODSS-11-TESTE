package com.sustentafome.sustentafome.donation;

import com.sustentafome.sustentafome.inventory.InventoryService;
import com.sustentafome.sustentafome.inventory.ReservaRequest;
import com.sustentafome.sustentafome.production.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final EntidadeBeneficiariaRepository beneficiariaRepository;
    private final CampanhaDoacaoRepository campanhaRepository;
    private final PedidoDoacaoRepository pedidoRepository;
    private final RotaEntregaRepository rotaRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoRepository veiculoRepository;
    private final EntregaRepository entregaRepository;
    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    @Transactional
    public PedidoDoacao criarPedido(PedidoRequest request) {
        var beneficiario = beneficiariaRepository.findById(request.beneficiarioId())
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario nao encontrado"));
        var campanha = request.campanhaId() != null ? campanhaRepository.findById(request.campanhaId()).orElse(null) : null;
        var produto = productRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado"));
        PedidoDoacao pedido = new PedidoDoacao();
        pedido.setBeneficiario(beneficiario);
        pedido.setCampanha(campanha);
        pedido.setProduto(produto);
        pedido.setQuantidade(request.quantidade());
        pedido.setStatus(PedidoStatus.ABERTO);
        pedido.setDataPedido(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoDoacao reservarPedido(Long pedidoId, Long loteId) {
        PedidoDoacao pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        inventoryService.reservar(new ReservaRequest(pedido.getProduto().getId(), loteId, pedido.getQuantidade(), pedidoId));
        pedido.setStatus(PedidoStatus.RESERVADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoDoacao avancarStatus(Long pedidoId, PedidoStatus novoStatus) {
        PedidoDoacao pedido = pedidoRepository.findById(pedidoId).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Entrega criarEntrega(EntregaRequest request) {
        PedidoDoacao pedido = pedidoRepository.findById(request.pedidoId()).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
        RotaEntrega rota = request.rotaId() != null ? rotaRepository.findById(request.rotaId()).orElse(null) : null;
        Motorista mot = request.motoristaId() != null ? motoristaRepository.findById(request.motoristaId()).orElse(null) : null;
        Veiculo veiculo = request.veiculoId() != null ? veiculoRepository.findById(request.veiculoId()).orElse(null) : null;

        Entrega entrega = new Entrega();
        entrega.setPedido(pedido);
        entrega.setRota(rota);
        entrega.setMotorista(mot);
        entrega.setVeiculo(veiculo);
        entrega.setDataSaida(request.dataSaida());
        entrega.setDataEntrega(request.dataEntrega());
        entrega.setStatus(EntregaStatus.PENDENTE);
        return entregaRepository.save(entrega);
    }

    @Transactional
    public Entrega confirmarEntrega(Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElseThrow(() -> new IllegalArgumentException("Entrega nao encontrada"));
        entrega.setStatus(EntregaStatus.ENTREGUE);
        entrega.setDataEntrega(LocalDateTime.now());
        if (entrega.getPedido() != null) {
            entrega.getPedido().setStatus(PedidoStatus.ENTREGUE);
            pedidoRepository.save(entrega.getPedido());
        }
        return entregaRepository.save(entrega);
    }
}
