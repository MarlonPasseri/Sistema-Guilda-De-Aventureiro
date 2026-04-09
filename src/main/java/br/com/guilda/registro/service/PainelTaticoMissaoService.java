package br.com.guilda.registro.service;

import br.com.guilda.registro.config.RuntimePropertyKeys;
import br.com.guilda.registro.domain.PainelTaticoMissao;
import br.com.guilda.registro.dto.PainelTaticoMissaoResponse;
import br.com.guilda.registro.repository.PainelTaticoMissaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PainelTaticoMissaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PainelTaticoMissaoService.class);
    private static final int JANELA_EM_DIAS = 15;
    private static final int LIMITE_RESULTADOS = 10;
    private static final int TTL_PADRAO_SEGUNDOS = 300;

    private final PainelTaticoMissaoRepository painelTaticoMissaoRepository;
    private final Clock clock;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public PainelTaticoMissaoService(
        PainelTaticoMissaoRepository painelTaticoMissaoRepository,
        Clock clock,
        StringRedisTemplate stringRedisTemplate,
        ObjectMapper objectMapper,
        Environment environment
    ) {
        this.painelTaticoMissaoRepository = painelTaticoMissaoRepository;
        this.clock = clock;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    public List<PainelTaticoMissaoResponse> listarTopMissoesUltimos15Dias() {
        List<PainelTaticoMissaoResponse> rankingEmCache = buscarNoRedis();
        if (rankingEmCache != null) {
            return rankingEmCache;
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime dataLimite = agora.minusDays(JANELA_EM_DIAS);
        PageRequest pageRequest = PageRequest.of(
            0,
            LIMITE_RESULTADOS,
            Sort.by(
                Sort.Order.desc("indiceProntidao"),
                Sort.Order.desc("ultimaAtualizacao"),
                Sort.Order.desc("missaoId")
            )
        );

        List<PainelTaticoMissaoResponse> ranking = painelTaticoMissaoRepository.findByUltimaAtualizacaoBetween(dataLimite, agora, pageRequest)
            .stream()
            .map(this::toResponse)
            .toList();

        salvarNoRedis(ranking);
        return ranking;
    }

    private PainelTaticoMissaoResponse toResponse(PainelTaticoMissao missao) {
        return new PainelTaticoMissaoResponse(
            missao.getMissaoId(),
            missao.getTitulo(),
            missao.getStatus(),
            missao.getNivelPerigo(),
            missao.getOrganizacaoId(),
            missao.getTotalParticipantes(),
            missao.getNivelMedioEquipe(),
            missao.getTotalRecompensa(),
            missao.getTotalMvps(),
            missao.getParticipantesComCompanheiro(),
            missao.getUltimaAtualizacao(),
            missao.getIndiceProntidao()
        );
    }

    private List<PainelTaticoMissaoResponse> buscarNoRedis() {
        try {
            String payload = stringRedisTemplate.opsForValue().get(RuntimePropertyKeys.RANKING_CACHE_REDIS_KEY);
            if (payload == null || payload.isBlank()) {
                return null;
            }
            return objectMapper.readValue(payload, new TypeReference<List<PainelTaticoMissaoResponse>>() {
            });
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Falha ao desserializar cache Redis do ranking tatico: {}", ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            LOGGER.warn("Falha ao consultar Redis para o ranking tatico: {}", ex.getMessage());
            return null;
        }
    }

    private void salvarNoRedis(List<PainelTaticoMissaoResponse> ranking) {
        int ttlSegundos = environment.getProperty(
            RuntimePropertyKeys.RANKING_CACHE_TTL_SEGUNDOS,
            Integer.class,
            TTL_PADRAO_SEGUNDOS
        );
        if (ttlSegundos <= 0) {
            try {
                stringRedisTemplate.delete(RuntimePropertyKeys.RANKING_CACHE_REDIS_KEY);
            } catch (RuntimeException ex) {
                LOGGER.warn("Falha ao limpar cache Redis do ranking tatico: {}", ex.getMessage());
            }
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(ranking);
            stringRedisTemplate.opsForValue().set(
                RuntimePropertyKeys.RANKING_CACHE_REDIS_KEY,
                payload,
                Duration.ofSeconds(ttlSegundos)
            );
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Falha ao serializar cache Redis do ranking tatico: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            LOGGER.warn("Falha ao gravar ranking tatico no Redis: {}", ex.getMessage());
        }
    }
}
