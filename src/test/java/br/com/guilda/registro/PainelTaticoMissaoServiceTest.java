package br.com.guilda.registro;

import br.com.guilda.registro.domain.PainelTaticoMissao;
import br.com.guilda.registro.dto.PainelTaticoMissaoResponse;
import br.com.guilda.registro.repository.PainelTaticoMissaoRepository;
import br.com.guilda.registro.service.PainelTaticoMissaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelTaticoMissaoServiceTest {

    @Mock
    private PainelTaticoMissaoRepository painelTaticoMissaoRepository;

    @Captor
    private ArgumentCaptor<LocalDateTime> dataLimiteCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> agoraCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Environment environment;

    @Test
    void deveBuscarTop10MissoesDosUltimos15DiasOrdenadasPorIndiceDeProntidao() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-09T12:00:00Z"), ZoneOffset.UTC);
        PainelTaticoMissaoService service = new PainelTaticoMissaoService(
            painelTaticoMissaoRepository,
            clock,
            stringRedisTemplate,
            new ObjectMapper().findAndRegisterModules(),
            environment
        );

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);
        when(environment.getProperty(eq("guilda.cache.ranking.ttl-segundos"), eq(Integer.class), eq(300))).thenReturn(300);

        when(painelTaticoMissaoRepository.findByUltimaAtualizacaoBetween(any(), any(), any()))
            .thenReturn(List.of(
                criarMissao(10L, "Muralha do Norte", "98.50"),
                criarMissao(11L, "Templo Rubro", "91.25")
            ));

        List<PainelTaticoMissaoResponse> resultado = service.listarTopMissoesUltimos15Dias();

        verify(painelTaticoMissaoRepository).findByUltimaAtualizacaoBetween(
            dataLimiteCaptor.capture(),
            agoraCaptor.capture(),
            pageableCaptor.capture()
        );

        assertThat(dataLimiteCaptor.getValue()).isEqualTo(LocalDateTime.parse("2026-03-25T12:00:00"));
        assertThat(agoraCaptor.getValue()).isEqualTo(LocalDateTime.parse("2026-04-09T12:00:00"));
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(pageableCaptor.getValue().getSort()).extracting(Object::toString)
            .containsExactly(
                "indiceProntidao: DESC",
                "ultimaAtualizacao: DESC",
                "missaoId: DESC"
            );
        assertThat(resultado).extracting(PainelTaticoMissaoResponse::titulo)
            .containsExactly("Muralha do Norte", "Templo Rubro");
        assertThat(resultado).extracting(PainelTaticoMissaoResponse::indiceProntidao)
            .containsExactly(new BigDecimal("98.50"), new BigDecimal("91.25"));

        verify(valueOperations).set(any(), any(), eq(java.time.Duration.ofSeconds(300)));
    }

    private PainelTaticoMissao criarMissao(Long missaoId, String titulo, String indiceProntidao) {
        return new PainelTaticoMissao(
            missaoId,
            titulo,
            "CONCLUIDA",
            "EPICO",
            1L,
            4L,
            new BigDecimal("18.75"),
            new BigDecimal("1500.00"),
            2L,
            3L,
            LocalDateTime.parse("2026-04-08T10:00:00"),
            new BigDecimal(indiceProntidao)
        );
    }
}
