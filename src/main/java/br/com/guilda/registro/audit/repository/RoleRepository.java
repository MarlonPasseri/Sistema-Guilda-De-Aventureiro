package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = {"organizacao", "permissions"})
    Optional<Role> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"organizacao", "permissions"})
    List<Role> findAllByOrganizacao_IdOrderByNomeAsc(Long organizacaoId);
}
