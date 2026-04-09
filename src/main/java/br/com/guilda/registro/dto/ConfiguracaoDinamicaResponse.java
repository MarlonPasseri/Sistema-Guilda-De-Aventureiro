package br.com.guilda.registro.dto;

import java.util.List;

public record ConfiguracaoDinamicaResponse(
    Integer ttlRankingSegundos,
    boolean historicoMongoHabilitado,
    List<AutoconfiguracaoDiagnosticoResponse> diagnosticosPersistencia
) {
}
