package br.com.guilda.registro.dto;

import java.time.OffsetDateTime;

public record MissaoResumoResponse(
    Long id,
    String titulo,
    String status,
    String nivelPerigo,
    OffsetDateTime createdAt,
    OffsetDateTime dataInicio,
    OffsetDateTime dataTermino
) {
}
