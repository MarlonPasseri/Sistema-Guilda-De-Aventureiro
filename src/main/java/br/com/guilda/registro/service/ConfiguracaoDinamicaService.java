package br.com.guilda.registro.service;

import br.com.guilda.registro.config.RuntimePropertyKeys;
import br.com.guilda.registro.dto.AutoconfiguracaoDiagnosticoResponse;
import br.com.guilda.registro.dto.ConfiguracaoDinamicaResponse;
import br.com.guilda.registro.exception.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConfiguracaoDinamicaService {

    private static final int TTL_PADRAO_SEGUNDOS = 300;

    private final ConfigurableApplicationContext applicationContext;
    private final ConfigurableEnvironment environment;

    public ConfiguracaoDinamicaService(
        ConfigurableApplicationContext applicationContext,
        ConfigurableEnvironment environment
    ) {
        this.applicationContext = applicationContext;
        this.environment = environment;
    }

    public ConfiguracaoDinamicaResponse consultarConfiguracoes() {
        return new ConfiguracaoDinamicaResponse(
            getTtlRankingSegundos(),
            historicoMongoHabilitado(),
            diagnosticarPersistencia()
        );
    }

    public ConfiguracaoDinamicaResponse atualizarConfiguracoes(Integer ttlRankingSegundos, Boolean historicoMongoHabilitado) {
        if (ttlRankingSegundos != null && ttlRankingSegundos < 0) {
            throw ApiException.invalid(List.of("ttlRankingSegundos nao pode ser negativo"));
        }

        Map<String, Object> propriedadesDinamicas = obterOuCriarPropriedadesDinamicas();
        if (ttlRankingSegundos != null) {
            propriedadesDinamicas.put(RuntimePropertyKeys.RANKING_CACHE_TTL_SEGUNDOS, ttlRankingSegundos);
        }
        if (historicoMongoHabilitado != null) {
            propriedadesDinamicas.put(RuntimePropertyKeys.HISTORICO_MONGO_HABILITADO, historicoMongoHabilitado);
        }
        return consultarConfiguracoes();
    }

    public int getTtlRankingSegundos() {
        return environment.getProperty(RuntimePropertyKeys.RANKING_CACHE_TTL_SEGUNDOS, Integer.class, TTL_PADRAO_SEGUNDOS);
    }

    public boolean historicoMongoHabilitado() {
        return environment.getProperty(RuntimePropertyKeys.HISTORICO_MONGO_HABILITADO, Boolean.class, true);
    }

    public List<AutoconfiguracaoDiagnosticoResponse> diagnosticarPersistencia() {
        ConditionEvaluationReport report = ConditionEvaluationReport.get(applicationContext.getBeanFactory());
        return List.of(
            "HibernateJpaAutoConfiguration",
            "MongoAutoConfiguration",
            "MongoDataAutoConfiguration",
            "MongoRepositoriesAutoConfiguration",
            "RedisAutoConfiguration",
            "RedisRepositoriesAutoConfiguration"
        ).stream().map(simpleName -> toDiagnostico(report, simpleName)).toList();
    }

    private AutoconfiguracaoDiagnosticoResponse toDiagnostico(ConditionEvaluationReport report, String simpleName) {
        Optional<Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes>> source = report.getConditionAndOutcomesBySource()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().endsWith(simpleName))
            .findFirst();

        if (source.isEmpty()) {
            return new AutoconfiguracaoDiagnosticoResponse(
                simpleName,
                false,
                List.of("Auto-configuracao nao encontrada no relatorio atual")
            );
        }

        List<String> mensagens = new ArrayList<>();
        source.get().getValue().forEach(conditionAndOutcome -> {
            String mensagem = conditionAndOutcome.getOutcome().getMessage();
            if (mensagem != null && !mensagem.isBlank()) {
                mensagens.add(mensagem);
            }
        });

        List<String> detalhes = mensagens.isEmpty()
            ? List.of("Sem detalhes adicionais no relatorio")
            : List.copyOf(mensagens);

        return new AutoconfiguracaoDiagnosticoResponse(
            simpleName,
            source.get().getValue().isFullMatch(),
            detalhes
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> obterOuCriarPropriedadesDinamicas() {
        MapPropertySource propertySource = (MapPropertySource) environment.getPropertySources()
            .stream()
            .filter(source -> RuntimePropertyKeys.DYNAMIC_PROPERTY_SOURCE.equals(source.getName()))
            .findFirst()
            .orElseGet(() -> {
                MapPropertySource novoSource = new MapPropertySource(
                    RuntimePropertyKeys.DYNAMIC_PROPERTY_SOURCE,
                    new LinkedHashMap<>()
                );
                environment.getPropertySources().addFirst(novoSource);
                return novoSource;
            });
        return (Map<String, Object>) propertySource.getSource();
    }
}
