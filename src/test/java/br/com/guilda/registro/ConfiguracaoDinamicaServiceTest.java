package br.com.guilda.registro;

import br.com.guilda.registro.service.ConfiguracaoDinamicaService;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoDinamicaServiceTest {

    @Test
    void deveAtualizarConfiguracoesDinamicasEmTempoDeExecucao() {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.refresh();

        ConfiguracaoDinamicaService service = new ConfiguracaoDinamicaService(
            applicationContext,
            applicationContext.getEnvironment()
        );

        var atualizado = service.atualizarConfiguracoes(120, false);

        assertThat(atualizado.ttlRankingSegundos()).isEqualTo(120);
        assertThat(atualizado.historicoMongoHabilitado()).isFalse();
        assertThat(applicationContext.getEnvironment().getProperty("guilda.cache.ranking.ttl-segundos")).isEqualTo("120");
        assertThat(applicationContext.getEnvironment().getProperty("guilda.marketplace.historico.mongo.habilitado")).isEqualTo("false");
        assertThat(atualizado.diagnosticosPersistencia()).isNotEmpty();
    }
}
