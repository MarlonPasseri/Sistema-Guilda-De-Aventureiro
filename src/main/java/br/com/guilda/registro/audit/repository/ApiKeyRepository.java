package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
}
