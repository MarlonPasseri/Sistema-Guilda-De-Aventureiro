package br.com.guilda.registro.dto;

public record AventureiroResumoResponse(
    Long id,
    String nome,
    String classe,
    Integer nivel,
    boolean ativo
) {
}
