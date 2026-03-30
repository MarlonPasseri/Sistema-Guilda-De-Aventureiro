package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.Companheiro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanheiroRepository extends JpaRepository<Companheiro, Long> {
}
