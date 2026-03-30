package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
