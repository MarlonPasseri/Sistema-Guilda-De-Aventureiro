package br.com.guilda.registro.repository;

import br.com.guilda.registro.domain.PainelTaticoMissao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PainelTaticoMissaoRepository extends JpaRepository<PainelTaticoMissao, Long> {

    List<PainelTaticoMissao> findByUltimaAtualizacaoBetween(LocalDateTime dataLimite, LocalDateTime agora, Pageable pageable);
}
