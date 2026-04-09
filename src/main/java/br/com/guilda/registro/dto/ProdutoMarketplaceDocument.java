package br.com.guilda.registro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProdutoMarketplaceDocument(
    String nome,
    String descricao,
    String categoria,
    String raridade,
    BigDecimal preco
) {
}
