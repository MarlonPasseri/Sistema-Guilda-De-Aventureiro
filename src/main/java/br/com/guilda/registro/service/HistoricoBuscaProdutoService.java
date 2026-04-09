package br.com.guilda.registro.service;

import br.com.guilda.registro.config.RuntimePropertyKeys;
import br.com.guilda.registro.dto.HistoricoBuscaProdutoResponse;
import br.com.guilda.registro.exception.ApiException;
import br.com.guilda.registro.mongodb.domain.HistoricoBuscaProduto;
import br.com.guilda.registro.mongodb.repository.HistoricoBuscaProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class HistoricoBuscaProdutoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricoBuscaProdutoService.class);

    private final HistoricoBuscaProdutoRepository historicoBuscaProdutoRepository;
    private final Environment environment;
    private final Clock clock;

    public HistoricoBuscaProdutoService(
        HistoricoBuscaProdutoRepository historicoBuscaProdutoRepository,
        Environment environment,
        Clock clock
    ) {
        this.historicoBuscaProdutoRepository = historicoBuscaProdutoRepository;
        this.environment = environment;
        this.clock = clock;
    }

    public void registrarBusca(
        String tipoBusca,
        String termo,
        String categoria,
        String raridade,
        BigDecimal precoMinimo,
        BigDecimal precoMaximo,
        int quantidadeResultados
    ) {
        if (!historicoMongoHabilitado()) {
            return;
        }

        HistoricoBuscaProduto historico = new HistoricoBuscaProduto();
        historico.setTipoBusca(tipoBusca);
        historico.setTermo(termo);
        historico.setCategoria(categoria);
        historico.setRaridade(raridade);
        historico.setPrecoMinimo(precoMinimo);
        historico.setPrecoMaximo(precoMaximo);
        historico.setQuantidadeResultados(quantidadeResultados);
        historico.setCriadoEm(OffsetDateTime.now(clock));

        try {
            historicoBuscaProdutoRepository.save(historico);
        } catch (RuntimeException ex) {
            LOGGER.warn("Nao foi possivel registrar historico da busca no MongoDB: {}", ex.getMessage());
        }
    }

    public List<HistoricoBuscaProdutoResponse> listarUltimasBuscas(String tipoBusca) {
        try {
            List<HistoricoBuscaProduto> historicos = (tipoBusca == null || tipoBusca.isBlank())
                ? historicoBuscaProdutoRepository.findTop10ByOrderByCriadoEmDesc()
                : historicoBuscaProdutoRepository.findTop10ByTipoBuscaOrderByCriadoEmDesc(tipoBusca.trim());

            return historicos.stream()
                .map(this::toResponse)
                .toList();
        } catch (RuntimeException ex) {
            throw new ApiException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "Servico NoSQL indisponivel",
                List.of("Nao foi possivel consultar o historico de buscas no MongoDB")
            );
        }
    }

    public boolean historicoMongoHabilitado() {
        return environment.getProperty(RuntimePropertyKeys.HISTORICO_MONGO_HABILITADO, Boolean.class, true);
    }

    private HistoricoBuscaProdutoResponse toResponse(HistoricoBuscaProduto historico) {
        return new HistoricoBuscaProdutoResponse(
            historico.getId(),
            historico.getTipoBusca(),
            historico.getTermo(),
            historico.getCategoria(),
            historico.getRaridade(),
            historico.getPrecoMinimo(),
            historico.getPrecoMaximo(),
            historico.getQuantidadeResultados(),
            historico.getCriadoEm()
        );
    }
}
