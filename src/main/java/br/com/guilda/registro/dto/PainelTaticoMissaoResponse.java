package br.com.guilda.registro.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PainelTaticoMissaoResponse(
    Long missaoId,
    String titulo,
    String status,
    String nivelPerigo,
    Long organizacaoId,
    Long totalParticipantes,
    BigDecimal nivelMedioEquipe,
    BigDecimal totalRecompensa,
    Long totalMvps,
    Long participantesComCompanheiro,
    LocalDateTime ultimaAtualizacao,
    BigDecimal indiceProntidao
) {
}
