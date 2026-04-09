package br.com.guilda.registro.mongodb.repository;

import br.com.guilda.registro.mongodb.domain.HistoricoBuscaProduto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoricoBuscaProdutoRepository extends MongoRepository<HistoricoBuscaProduto, String> {

    List<HistoricoBuscaProduto> findTop10ByOrderByCriadoEmDesc();

    List<HistoricoBuscaProduto> findTop10ByTipoBuscaOrderByCriadoEmDesc(String tipoBusca);
}
