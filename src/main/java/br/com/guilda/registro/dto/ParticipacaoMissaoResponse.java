package br.com.guilda.registro.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ParticipacaoMissaoResponse(
    Long aventureiroId,
    String aventureiroNome,
    String papelNaMissao,
    BigDecimal recompensaOuro,
    boolean destaque,
    OffsetDateTime dataRegistro
) {
}
