package br.com.guilda.registro.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MissaoDetalheResponse(
    Long id,
    Long organizacaoId,
    String titulo,
    String status,
    String nivelPerigo,
    OffsetDateTime createdAt,
    OffsetDateTime dataInicio,
    OffsetDateTime dataTermino,
    List<ParticipacaoMissaoResponse> participantes
) {
}
