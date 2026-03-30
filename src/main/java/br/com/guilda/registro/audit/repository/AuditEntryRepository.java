package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
}
