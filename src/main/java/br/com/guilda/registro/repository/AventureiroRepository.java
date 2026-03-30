package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.Aventureiro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AventureiroRepository extends JpaRepository<Aventureiro, Long>, JpaSpecificationExecutor<Aventureiro> {

    @EntityGraph(attributePaths = {"organizacao", "usuarioCadastro", "companheiro"})
    Optional<Aventureiro> findDetailedByIdAndOrganizacao_Id(Long id, Long organizacaoId);
}
