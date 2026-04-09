# Relatorio de Implementacao e Evidencias

## 1. Criacao do projeto com Spring Boot

### Ferramenta de build escolhida

Foi escolhido **Maven**.

Justificativa:

- integracao nativa e previsivel com o ecossistema Spring Boot
- convencao clara de ciclo de vida (`compile`, `test`, `package`)
- facilidade para empacotar e publicar artefatos JAR
- ampla compatibilidade com IDEs e pipelines CI/CD

### Spring Initializr via interface web

Configuracao documentada para reproducao:

- Project: `Maven`
- Language: `Java`
- Spring Boot: `3.3.3`
- Group: `br.com.guilda`
- Artifact: `registro-guilda`
- Java: `17`
- Dependencies:
  - Spring Web
  - Spring Data JPA
  - Validation
  - Actuator
  - Spring Security
  - Spring Data Redis
  - Spring Data MongoDB
  - Spring Data Elasticsearch
  - PostgreSQL Driver

URL equivalente de reproducao:

```text
https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.3.3&packaging=jar&jvmVersion=17&groupId=br.com.guilda&artifactId=registro-guilda&name=registro-guilda&dependencies=web,data-jpa,validation,actuator,security,data-redis,data-mongodb,data-elasticsearch,postgresql
```

### Spring Boot CLI

Comando de exemplo para iniciar o mesmo projeto via CLI:

```bash
spring init \
  --build=maven \
  --java-version=17 \
  --groupId=br.com.guilda \
  --artifactId=registro-guilda \
  --name=registro-guilda \
  --dependencies=web,data-jpa,validation,actuator,security,data-redis,data-mongodb,data-elasticsearch,postgresql \
  registro-guilda
```

### Quando usar cada metodo

- **Spring Initializr Web**: melhor para inicio guiado, onboarding e escolha rapida de dependencias.
- **Spring Boot CLI**: melhor para automacao, scripts, workshops, padronizacao de turmas e reproducao em linha de comando.
- **IDE**: melhor quando o time quer iniciar e importar o projeto no mesmo fluxo de desenvolvimento.

### Gerenciamento de dependencias

Abordagem utilizada:

- `spring-boot-starter-parent` para alinhamento de versoes
- starters para reduzir dependencia manual e conflitos
- dependencias de teste isoladas com `scope test`
- drivers externos apenas quando realmente necessarios

Beneficios para o ciclo de vida:

- menos incompatibilidade entre bibliotecas
- build mais previsivel
- manutencao simplificada
- upgrade centralizado pela versao do Spring Boot

### Autoconfiguracao

O projeto utiliza autoconfiguracao do Spring Boot por meio de:

- `@SpringBootApplication`
- starters oficiais
- configuracao por propriedades em `application.properties`
- `ConditionEvaluationReport` e Actuator para diagnostico

Resultado:

- menos codigo manual para servlet container, Jackson, MVC, DataSource, JPA, Redis, Mongo, Elasticsearch e Security

### Configuracao da IDE

Foi incluida configuracao do VS Code no repositorio:

- `.vscode/settings.json`
- `.vscode/launch.json`
- `.vscode/tasks.json`
- `.vscode/extensions.json`

Esses arquivos padronizam:

- importacao Maven
- extensoes Spring/Java recomendadas
- execucao local da aplicacao
- tarefas de teste e empacotamento

## 2. Desenvolvimento das APIs REST

### Organizacao do codigo

Estrutura adotada:

- `controller`: rotas HTTP
- `service`: regras de negocio
- `repository`: persistencia
- `domain`: entidades JPA e documentos Mongo
- `dto`: contratos de entrada e saida
- `exception`: tratamento padronizado de erros

### Rotas implementadas

Servicos REST principais:

- `AuditController`
- `AventureiroController`
- `MissaoController`
- `RelatorioController`
- `PainelTaticoMissaoController`
- `ProdutoMarketplaceController`
- `HistoricoBuscaProdutoController`
- `DiagnosticoController`

### Regras de integridade aplicadas

- atualizacao de aventureiro preserva escopo da organizacao
- participacao em missao respeita status e unicidade
- encerramento de vinculo e remocao de companheiro mantem consistencia do dominio
- erros invalidos, conflitos e nao encontrados retornam respostas coerentes

### Exemplos de chamadas e respostas

#### Endpoint publico

```bash
curl http://localhost:8080/status
```

Resposta validada em 2026-04-09:

```json
{
  "relatorios": "GET /relatorios/aventureiros/ranking?organizacaoId=1",
  "historicoMongo": "GET /produtos/buscas/historico",
  "aventuras": "GET /aventureiros?organizacaoId=1",
  "rankingRedis": "GET /missoes/top15dias",
  "diagnosticos": "GET /diagnosticos/autoconfiguracao/persistencia",
  "auditUsuarios": "GET /audit/usuarios?organizacaoId=1",
  "auditRoles": "GET /audit/roles?organizacaoId=1",
  "mensagem": "API da Guilda em execucao",
  "missoes": "GET /missoes?organizacaoId=1"
}
```

#### Endpoint protegido com usuario operador

```bash
curl -u operador:GuildaOperador@123 "http://localhost:8080/missoes/top15dias"
```

Resposta validada em 2026-04-09:

```json
[
  {
    "missaoId": 45,
    "titulo": "Missao_45",
    "status": "PLANEJADA",
    "indiceProntidao": 57.0
  },
  {
    "missaoId": 72,
    "titulo": "Missao_72",
    "status": "CONCLUIDA",
    "indiceProntidao": 53.0
  }
]
```

#### Endpoint administrativo

```bash
curl -u estrategista:GuildaAdmin@123 "http://localhost:8080/diagnosticos/configuracoes"
```

Resposta validada em 2026-04-09:

```json
{
  "ttlRankingSegundos": 300,
  "historicoMongoHabilitado": true,
  "diagnosticosPersistencia": [
    {
      "autoConfiguracao": "HibernateJpaAutoConfiguration",
      "ativa": true
    },
    {
      "autoConfiguracao": "MongoAutoConfiguration",
      "ativa": true
    },
    {
      "autoConfiguracao": "RedisAutoConfiguration",
      "ativa": true
    }
  ]
}
```

### Repositorio Git

Repositorio remoto configurado:

```text
https://github.com/MarlonPasseri/Sistema-Guilda-De-Aventureiro.git
```

## 3. Persistencia com JPA, Redis e MongoDB

### JPA

- entidades mapeadas com `@Entity`
- repositrios com `JpaRepository`
- filtros dinamicos com `Specification`
- consultas paginadas, filtradas e ordenadas

### Redis

- cache do ranking tatico usando `StringRedisTemplate`
- chave `missoes:top15dias`
- TTL dinamico configuravel

### MongoDB

- historico de buscas salvo como documento
- repositrio `MongoRepository`
- consulta do historico por endpoint

### Configuracao dinamica

- uso de `ConfigurableEnvironment`
- uso de `ConditionEvaluationReport`
- endpoint para atualizar propriedades em runtime

## 4. Testes

### Piramide de testes aplicada

- testes unitarios para services isolados
- slices `@DataJpaTest` para persistencia relacional
- teste de integracao com `@SpringBootTest` para contexto completo e seguranca

### Classes de teste em destaque

- `AuditMappingRepositoryTest`
- `AventureiroQueryServiceTest`
- `MissaoRelatorioServiceTest`
- `PainelTaticoMissaoServiceTest`
- `PainelTaticoMissaoRedisCacheTest`
- `HistoricoBuscaProdutoServiceTest`
- `ConfiguracaoDinamicaServiceTest`
- `ApplicationSecurityIntegrationTest`

### Execucao validada

Comandos executados com sucesso durante a validacao final:

```bash
mvn package
docker compose up -d guilda-db guilda-es guilda-redis guilda-mongo guilda-app
curl http://localhost:8080/actuator/health
curl -u operador:GuildaOperador@123 http://localhost:8080/missoes/top15dias
```

## 5. Seguranca

### Estrategia aplicada

- Spring Security com HTTP Basic
- autenticacao via `InMemoryUserDetailsManager`
- autorizacao por rota
- customizacao da autoconfiguracao padrao com `SecurityFilterChain`

### Perfis de acesso

- `estrategista`: acesso administrativo e operacional
- `operador`: acesso operacional

### Endpoints publicos

- `/status`
- `/actuator/health`
- `/actuator/info`

## 6. Build e deploy

### Empacotamento

Comandos utilizados:

```bash
mvn test
mvn package
java -jar target/registro-guilda-0.0.1-SNAPSHOT.jar
```

### Verificacao de integridade

- `GET /actuator/health`
- teste de integracao com `@SpringBootTest`
- `docker compose ps` com `guilda-app` em estado `healthy`

### Docker

Arquivos incluidos:

- `Dockerfile`
- `docker-compose.yml`

Containers previstos:

- PostgreSQL
- Elasticsearch
- Redis
- MongoDB
- aplicacao Spring Boot

### Resultado final da validacao

Em 2026-04-09 a aplicacao foi validada com:

- JAR gerado em `target/registro-guilda-0.0.1-SNAPSHOT.jar`
- execucao autonoma via `java -jar target/registro-guilda-0.0.1-SNAPSHOT.jar --server.port=8081`
- health check retornando `{"status":"UP"}`
- endpoint protegido `/missoes/top15dias` respondendo com autenticacao do operador
- endpoint administrativo `/diagnosticos/configuracoes` respondendo com autenticacao do admin
- endpoint `/audit/usuarios` retornando `401` sem credenciais
- stack completa em Docker com PostgreSQL, Elasticsearch, Redis, MongoDB e aplicacao Spring Boot

## Capturas

As capturas de tela geradas para a entrega ficam em:

- `docs/imagens/01-ide-config.png`
- `docs/imagens/02-build-e-execucao.png`
- `docs/imagens/03-respostas-api.png`
