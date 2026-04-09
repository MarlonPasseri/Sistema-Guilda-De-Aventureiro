package br.com.guilda.registro;

import br.com.guilda.registro.config.RuntimePropertyKeys;
import br.com.guilda.registro.dto.PainelTaticoMissaoResponse;
import br.com.guilda.registro.repository.PainelTaticoMissaoRepository;
import br.com.guilda.registro.service.PainelTaticoMissaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PainelTaticoMissaoRedisCacheTest {

    @Mock
    private PainelTaticoMissaoRepository painelTaticoMissaoRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Environment environment;

    @Test
    void deveReutilizarCacheDoRedisSemConsultarRepositorio() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        PainelTaticoMissaoResponse cachedItem = new PainelTaticoMissaoResponse(
            25L,
            "Posto Celeste",
            "EM_ANDAMENTO",
            "ALTO",
            1L,
            5L,
            new BigDecimal("16.20"),
            new BigDecimal("980.00"),
            1L,
            2L,
            LocalDateTime.parse("2026-04-08T14:30:00"),
            new BigDecimal("88.10")
        );

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RuntimePropertyKeys.RANKING_CACHE_REDIS_KEY))
            .thenReturn(objectMapper.writeValueAsString(List.of(cachedItem)));

        PainelTaticoMissaoService service = new PainelTaticoMissaoService(
            painelTaticoMissaoRepository,
            Clock.fixed(Instant.parse("2026-04-09T12:00:00Z"), ZoneOffset.UTC),
            stringRedisTemplate,
            objectMapper,
            environment
        );

        List<PainelTaticoMissaoResponse> resultado = service.listarTopMissoesUltimos15Dias();

        assertThat(resultado).containsExactly(cachedItem);
        verify(painelTaticoMissaoRepository, never()).findByUltimaAtualizacaoBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
