package br.com.guilda.registro.audit.repository;

import br.com.guilda.registro.audit.domain.Organizacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {
}
