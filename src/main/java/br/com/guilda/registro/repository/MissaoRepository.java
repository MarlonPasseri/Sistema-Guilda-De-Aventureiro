package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.Missao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MissaoRepository extends JpaRepository<Missao, Long>, JpaSpecificationExecutor<Missao> {

    @EntityGraph(attributePaths = {"organizacao", "participacoes", "participacoes.aventureiro"})
    Optional<Missao> findDetailedByIdAndOrganizacao_Id(Long id, Long organizacaoId);
}
