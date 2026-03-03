package br.com.guilda.registro.dto;

import br.com.guilda.registro.domain.EspecieCompanheiro;
import br.com.guilda.registro.validation.EnumValue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CompanheiroRequest {

    @NotBlank(message = "nome do companheiro e obrigatorio")
    private String nome;

    @NotBlank(message = "especie e obrigatoria")
    @EnumValue(enumClass = EspecieCompanheiro.class, message = "especie invalida")
    private String especie;

    @NotNull(message = "lealdade e obrigatoria")
    @Min(value = 0, message = "lealdade deve estar entre 0 e 100")
    @Max(value = 100, message = "lealdade deve estar entre 0 e 100")
    private Integer lealdade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public Integer getLealdade() {
        return lealdade;
    }

    public void setLealdade(Integer lealdade) {
        this.lealdade = lealdade;
    }
}
