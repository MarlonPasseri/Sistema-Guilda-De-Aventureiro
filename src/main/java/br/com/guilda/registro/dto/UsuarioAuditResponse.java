package br.com.guilda.registro.dto;

import java.util.Set;

public record UsuarioAuditResponse(
    Long id,
    Long organizacaoId,
    String organizacaoNome,
    String nome,
    String email,
    String status,
    Set<String> roles
) {
}
