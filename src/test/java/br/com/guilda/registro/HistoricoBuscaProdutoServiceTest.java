package br.com.guilda.registro;

import br.com.guilda.registro.dto.HistoricoBuscaProdutoResponse;
import br.com.guilda.registro.mongodb.domain.HistoricoBuscaProduto;
import br.com.guilda.registro.mongodb.repository.HistoricoBuscaProdutoRepository;
import br.com.guilda.registro.service.HistoricoBuscaProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoBuscaProdutoServiceTest {

    @Mock
    private HistoricoBuscaProdutoRepository historicoBuscaProdutoRepository;

    @Mock
    private Environment environment;

    @Captor
    private ArgumentCaptor<HistoricoBuscaProduto> historicoCaptor;

    @Test
    void devePersistirHistoricoDeBuscaNoMongoViaRepositorio() {
        when(environment.getProperty(eq("guilda.marketplace.historico.mongo.habilitado"), eq(Boolean.class), eq(true)))
            .thenReturn(true);

        HistoricoBuscaProdutoService service = new HistoricoBuscaProdutoService(
            historicoBuscaProdutoRepository,
            environment,
            Clock.fixed(Instant.parse("2026-04-09T12:00:00Z"), ZoneOffset.UTC)
        );

        service.registrarBusca(
            "nome",
            "espada",
            null,
            null,
            null,
            null,
            4
        );

        verify(historicoBuscaProdutoRepository).save(historicoCaptor.capture());
        assertThat(historicoCaptor.getValue().getTipoBusca()).isEqualTo("nome");
        assertThat(historicoCaptor.getValue().getTermo()).isEqualTo("espada");
        assertThat(historicoCaptor.getValue().getQuantidadeResultados()).isEqualTo(4);
        assertThat(historicoCaptor.getValue().getCriadoEm()).isEqualTo(OffsetDateTime.parse("2026-04-09T12:00:00Z"));
    }

    @Test
    void deveListarHistoricoMaisRecenteDoMongo() {
        HistoricoBuscaProduto item = new HistoricoBuscaProduto();
        item.setId("mongo-1");
        item.setTipoBusca("avancada");
        item.setCategoria("armas");
        item.setRaridade("raro");
        item.setPrecoMinimo(new BigDecimal("200.00"));
        item.setPrecoMaximo(new BigDecimal("1000.00"));
        item.setQuantidadeResultados(3);
        item.setCriadoEm(OffsetDateTime.parse("2026-04-09T10:15:00Z"));

        when(historicoBuscaProdutoRepository.findTop10ByOrderByCriadoEmDesc()).thenReturn(List.of(item));

        HistoricoBuscaProdutoService service = new HistoricoBuscaProdutoService(
            historicoBuscaProdutoRepository,
            environment,
            Clock.systemUTC()
        );

        List<HistoricoBuscaProdutoResponse> resultado = service.listarUltimasBuscas(null);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).tipoBusca()).isEqualTo("avancada");
        assertThat(resultado.get(0).categoria()).isEqualTo("armas");
    }
}
