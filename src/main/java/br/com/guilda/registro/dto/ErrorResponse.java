package br.com.guilda.registro.dto;

import java.util.List;

public record ErrorResponse(
    String mensagem,
    List<String> detalhes
) {
}
