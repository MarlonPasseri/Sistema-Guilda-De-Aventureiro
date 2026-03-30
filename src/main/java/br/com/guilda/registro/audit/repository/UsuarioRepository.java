package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"organizacao", "roles", "roles.permissions"})
    Optional<Usuario> findDetailedById(Long id);

    @EntityGraph(attributePaths = {"organizacao", "roles", "roles.permissions"})
    List<Usuario> findAllByOrganizacao_IdOrderByNomeAsc(Long organizacaoId);
}
