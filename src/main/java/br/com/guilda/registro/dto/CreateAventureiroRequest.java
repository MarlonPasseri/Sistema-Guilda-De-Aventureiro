package br.com.guilda.registro.dto;

import br.com.guilda.registro.domain.ClasseAventureiro;
import br.com.guilda.registro.validation.EnumValue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAventureiroRequest {

    @NotNull(message = "organizacaoId e obrigatorio")
    private Long organizacaoId;

    @NotNull(message = "usuarioCadastroId e obrigatorio")
    private Long usuarioCadastroId;

    @NotBlank(message = "nome e obrigatorio")
    private String nome;

    @NotBlank(message = "classe e obrigatoria")
    @EnumValue(enumClass = ClasseAventureiro.class, message = "classe invalida")
    private String classe;

    @NotNull(message = "nivel e obrigatorio")
    @Min(value = 1, message = "nivel deve ser maior ou igual a 1")
    private Integer nivel;

    public Long getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(Long organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public Long getUsuarioCadastroId() {
        return usuarioCadastroId;
    }

    public void setUsuarioCadastroId(Long usuarioCadastroId) {
        this.usuarioCadastroId = usuarioCadastroId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
}
