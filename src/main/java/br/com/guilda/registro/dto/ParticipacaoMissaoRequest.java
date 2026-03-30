package br.com.guilda.registro.dto;

import br.com.guilda.registro.domain.PapelMissao;
import br.com.guilda.registro.validation.EnumValue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ParticipacaoMissaoRequest {

    @NotNull(message = "aventureiroId e obrigatorio")
    private Long aventureiroId;

    @NotBlank(message = "papelNaMissao e obrigatorio")
    @EnumValue(enumClass = PapelMissao.class, message = "papelNaMissao invalido")
    private String papelNaMissao;

    @DecimalMin(value = "0.00", message = "recompensaOuro deve ser maior ou igual a zero")
    private BigDecimal recompensaOuro;

    @NotNull(message = "destaque e obrigatorio")
    private Boolean destaque;

    public Long getAventureiroId() {
        return aventureiroId;
    }

    public void setAventureiroId(Long aventureiroId) {
        this.aventureiroId = aventureiroId;
    }

    public String getPapelNaMissao() {
        return papelNaMissao;
    }

    public void setPapelNaMissao(String papelNaMissao) {
        this.papelNaMissao = papelNaMissao;
    }

    public BigDecimal getRecompensaOuro() {
        return recompensaOuro;
    }

    public void setRecompensaOuro(BigDecimal recompensaOuro) {
        this.recompensaOuro = recompensaOuro;
    }

    public Boolean getDestaque() {
        return destaque;
    }

    public void setDestaque(Boolean destaque) {
        this.destaque = destaque;
    }
}
