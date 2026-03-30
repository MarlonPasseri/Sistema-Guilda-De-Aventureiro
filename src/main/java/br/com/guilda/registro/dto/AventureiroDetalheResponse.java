package br.com.guilda.registro.dto;

public record AventureiroDetalheResponse(
    Long id,
    Long organizacaoId,
    Long usuarioCadastroId,
    String nome,
    String classe,
    Integer nivel,
    boolean ativo,
    CompanheiroResponse companheiro,
    long totalParticipacoes,
    UltimaMissaoResumoResponse ultimaMissao
) {
}
