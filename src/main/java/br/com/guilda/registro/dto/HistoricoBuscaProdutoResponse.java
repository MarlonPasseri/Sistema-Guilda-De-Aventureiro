package br.com.guilda.registro.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record HistoricoBuscaProdutoResponse(
    String id,
    String tipoBusca,
    String termo,
    String categoria,
    String raridade,
    BigDecimal precoMinimo,
    BigDecimal precoMaximo,
    Integer quantidadeResultados,
    OffsetDateTime criadoEm
) {
}
