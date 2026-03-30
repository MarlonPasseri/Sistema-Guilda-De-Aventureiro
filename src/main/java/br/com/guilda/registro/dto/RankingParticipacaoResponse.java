package br.com.guilda.registro.dto;

import java.math.BigDecimal;

public record RankingParticipacaoResponse(
    Long aventureiroId,
    String aventureiroNome,
    Long totalParticipacoes,
    BigDecimal totalRecompensas,
    Long totalDestaques
) {
}
