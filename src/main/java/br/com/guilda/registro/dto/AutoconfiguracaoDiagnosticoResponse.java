package br.com.guilda.registro.dto;

import java.util.List;

public record AutoconfiguracaoDiagnosticoResponse(
    String autoConfiguracao,
    boolean ativa,
    List<String> detalhes
) {
}
