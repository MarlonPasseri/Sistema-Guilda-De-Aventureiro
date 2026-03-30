package br.com.guilda.registro.dto;

import java.util.Set;

public record RoleAuditResponse(
    Long id,
    Long organizacaoId,
    String nome,
    String descricao,
    Set<String> permissions
) {
}
