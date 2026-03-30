package br.com.guilda.registro.dto;

import java.time.OffsetDateTime;

public record UltimaMissaoResumoResponse(
    Long id,
    String titulo,
    String status,
    OffsetDateTime dataRegistro
) {
}
