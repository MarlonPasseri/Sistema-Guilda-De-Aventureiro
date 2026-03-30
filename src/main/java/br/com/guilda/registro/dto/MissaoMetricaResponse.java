package br.com.guilda.registro.dto;

import br.com.guilda.registro.domain.NivelPerigoMissao;
import br.com.guilda.registro.domain.StatusMissao;

import java.math.BigDecimal;

public record MissaoMetricaResponse(
    Long missaoId,
    String titulo,
    StatusMissao status,
    NivelPerigoMissao nivelPerigo,
    Long quantidadeParticipantes,
    BigDecimal totalRecompensas
) {
}
