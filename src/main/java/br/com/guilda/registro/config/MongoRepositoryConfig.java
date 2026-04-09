package br.com.guilda.registro.config;

import br.com.guilda.registro.mongodb.repository.HistoricoBuscaProdutoRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackageClasses = HistoricoBuscaProdutoRepository.class)
public class MongoRepositoryConfig {
}
