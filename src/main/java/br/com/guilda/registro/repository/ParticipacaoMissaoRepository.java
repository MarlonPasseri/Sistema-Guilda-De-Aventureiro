package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.ParticipacaoMissao;
import br.com.guilda.registro.domain.ParticipacaoMissaoId;
import br.com.guilda.registro.domain.StatusMissao;
import br.com.guilda.registro.dto.MissaoMetricaResponse;
import br.com.guilda.registro.dto.RankingParticipacaoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ParticipacaoMissaoRepository extends JpaRepository<ParticipacaoMissao, ParticipacaoMissaoId> {

    boolean existsByMissao_IdAndAventureiro_Id(Long missaoId, Long aventureiroId);

    long countByAventureiro_Id(Long aventureiroId);

    Optional<ParticipacaoMissao> findFirstByAventureiro_IdOrderByDataRegistroDesc(Long aventureiroId);

    @Query("""
        select new br.com.guilda.registro.dto.RankingParticipacaoResponse(
            a.id,
            a.nome,
            count(p),
            coalesce(sum(p.recompensaOuro), 0),
            sum(case when p.destaque = true then 1 else 0 end)
        )
        from ParticipacaoMissao p
        join p.aventureiro a
        join p.missao m
        where a.organizacao.id = :organizacaoId
          and p.dataRegistro >= :inicio
          and p.dataRegistro <= :fim
        group by a.id, a.nome
        order by count(p) desc, coalesce(sum(p.recompensaOuro), 0) desc,
                 sum(case when p.destaque = true then 1 else 0 end) desc, a.nome asc
        """)
    List<RankingParticipacaoResponse> gerarRanking(
        @Param("organizacaoId") Long organizacaoId,
        @Param("inicio") OffsetDateTime inicio,
        @Param("fim") OffsetDateTime fim
    );

    @Query("""
        select new br.com.guilda.registro.dto.RankingParticipacaoResponse(
            a.id,
            a.nome,
            count(p),
            coalesce(sum(p.recompensaOuro), 0),
            sum(case when p.destaque = true then 1 else 0 end)
        )
        from ParticipacaoMissao p
        join p.aventureiro a
        join p.missao m
        where a.organizacao.id = :organizacaoId
          and p.dataRegistro >= :inicio
          and p.dataRegistro <= :fim
          and m.status = :statusMissao
        group by a.id, a.nome
        order by count(p) desc, coalesce(sum(p.recompensaOuro), 0) desc,
                 sum(case when p.destaque = true then 1 else 0 end) desc, a.nome asc
        """)
    List<RankingParticipacaoResponse> gerarRankingPorStatus(
        @Param("organizacaoId") Long organizacaoId,
        @Param("inicio") OffsetDateTime inicio,
        @Param("fim") OffsetDateTime fim,
        @Param("statusMissao") StatusMissao statusMissao
    );

    @Query("""
        select new br.com.guilda.registro.dto.MissaoMetricaResponse(
            m.id,
            m.titulo,
            m.status,
            m.nivelPerigo,
            count(p.aventureiro.id),
            coalesce(sum(p.recompensaOuro), 0)
        )
        from Missao m
        left join m.participacoes p
        where m.organizacao.id = :organizacaoId
          and m.createdAt >= :inicio
          and m.createdAt <= :fim
        group by m.id, m.titulo, m.status, m.nivelPerigo, m.createdAt
        order by m.createdAt desc, m.id desc
        """)
    List<MissaoMetricaResponse> relatorioMissoes(
        @Param("organizacaoId") Long organizacaoId,
        @Param("inicio") OffsetDateTime inicio,
        @Param("fim") OffsetDateTime fim
    );
}
