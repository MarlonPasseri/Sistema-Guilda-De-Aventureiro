package br.com.guilda.registro.dto;

import java.math.BigDecimal;

public record ProdutoMarketplaceResponse(
    String id,
    String nome,
    String descricao,
    String categoria,
    String raridade,
    BigDecimal preco
) {
}
