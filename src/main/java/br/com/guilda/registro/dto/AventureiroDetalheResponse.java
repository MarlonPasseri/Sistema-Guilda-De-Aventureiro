package br.com.guilda.registro.dto;

public record AventureiroDetalheResponse(
    Long id,
    String nome,
    String classe,
    Integer nivel,
    boolean ativo,
    CompanheiroResponse companheiro
) {
}
