package br.com.guilda.registro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "participacoes_missao", schema = "aventura")
public class ParticipacaoMissao {

    @EmbeddedId
    private ParticipacaoMissaoId id = new ParticipacaoMissaoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("missaoId")
    @JoinColumn(name = "missao_id", nullable = false)
    private Missao missao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("aventureiroId")
    @JoinColumn(name = "aventureiro_id", nullable = false)
    private Aventureiro aventureiro;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_na_missao", nullable = false, length = 30)
    private PapelMissao papelNaMissao;

    @Column(name = "recompensa_ouro", nullable = false, precision = 12, scale = 2)
    private BigDecimal recompensaOuro = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean destaque;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private OffsetDateTime dataRegistro;

    @PrePersist
    public void prePersist() {
        if (dataRegistro == null) {
            dataRegistro = OffsetDateTime.now();
        }
        if (recompensaOuro == null) {
            recompensaOuro = BigDecimal.ZERO;
        }
    }

    public ParticipacaoMissaoId getId() {
        return id;
    }

    public void setId(ParticipacaoMissaoId id) {
        this.id = id;
    }

    public Missao getMissao() {
        return missao;
    }

    public void setMissao(Missao missao) {
        this.missao = missao;
    }

    public Aventureiro getAventureiro() {
        return aventureiro;
    }

    public void setAventureiro(Aventureiro aventureiro) {
        this.aventureiro = aventureiro;
    }

    public PapelMissao getPapelNaMissao() {
        return papelNaMissao;
    }

    public void setPapelNaMissao(PapelMissao papelNaMissao) {
        this.papelNaMissao = papelNaMissao;
    }

    public BigDecimal getRecompensaOuro() {
        return recompensaOuro;
    }

    public void setRecompensaOuro(BigDecimal recompensaOuro) {
        this.recompensaOuro = recompensaOuro;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }

    public OffsetDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(OffsetDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}
