package br.com.guilda.registro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "vw_painel_tatico_missao", schema = "operacoes")
public class PainelTaticoMissao {

    @Id
    @Column(name = "missao_id")
    private Long missaoId;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "status")
    private String status;

    @Column(name = "nivel_perigo")
    private String nivelPerigo;

    @Column(name = "organizacao_id")
    private Long organizacaoId;

    @Column(name = "total_participantes")
    private Long totalParticipantes;

    @Column(name = "nivel_medio_equipe")
    private BigDecimal nivelMedioEquipe;

    @Column(name = "total_recompensa")
    private BigDecimal totalRecompensa;

    @Column(name = "total_mvps")
    private Long totalMvps;

    @Column(name = "participantes_com_companheiro")
    private Long participantesComCompanheiro;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "indice_prontidao")
    private BigDecimal indiceProntidao;

    protected PainelTaticoMissao() {
    }

    public PainelTaticoMissao(
        Long missaoId,
        String titulo,
        String status,
        String nivelPerigo,
        Long organizacaoId,
        Long totalParticipantes,
        BigDecimal nivelMedioEquipe,
        BigDecimal totalRecompensa,
        Long totalMvps,
        Long participantesComCompanheiro,
        LocalDateTime ultimaAtualizacao,
        BigDecimal indiceProntidao
    ) {
        this.missaoId = missaoId;
        this.titulo = titulo;
        this.status = status;
        this.nivelPerigo = nivelPerigo;
        this.organizacaoId = organizacaoId;
        this.totalParticipantes = totalParticipantes;
        this.nivelMedioEquipe = nivelMedioEquipe;
        this.totalRecompensa = totalRecompensa;
        this.totalMvps = totalMvps;
        this.participantesComCompanheiro = participantesComCompanheiro;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.indiceProntidao = indiceProntidao;
    }

    public Long getMissaoId() {
        return missaoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getStatus() {
        return status;
    }

    public String getNivelPerigo() {
        return nivelPerigo;
    }

    public Long getOrganizacaoId() {
        return organizacaoId;
    }

    public Long getTotalParticipantes() {
        return totalParticipantes;
    }

    public BigDecimal getNivelMedioEquipe() {
        return nivelMedioEquipe;
    }

    public BigDecimal getTotalRecompensa() {
        return totalRecompensa;
    }

    public Long getTotalMvps() {
        return totalMvps;
    }

    public Long getParticipantesComCompanheiro() {
        return participantesComCompanheiro;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public BigDecimal getIndiceProntidao() {
        return indiceProntidao;
    }
}
