# Sistema de Gestao de Aventureiros

Aplicacao Spring Boot para gerenciamento de aventureiros, missoes, auditoria, relatorios e servicos complementares da guilda.

O projeto integra:

- PostgreSQL com o schema legado `audit`
- schema novo `aventura`, criado pela aplicacao
- painel tatico de missoes com cache em Redis
- marketplace consultando o indice Elasticsearch `guilda_loja`
- historico de buscas persistido em MongoDB
- endpoints de diagnostico e configuracao dinamica em runtime
- interface web publica para exploracao rapida da API

## Visao geral

### Dominio principal

- cadastro e consulta de aventureiros
- associacao e remocao de companheiros
- criacao e detalhamento de missoes
- participacao de aventureiros em missoes
- relatorios agregados por aventureiro e por missao

### Modulos complementares

- auditoria de usuarios, roles, permissions, api keys e audit entries do schema legado
- painel tatico com leitura da view `operacoes.vw_painel_tatico_missao`
- cache do ranking tatico com `StringRedisTemplate`
- buscas textuais e agregacoes no Elasticsearch
- historico de buscas salvo em MongoDB
- leitura de auto-configuracoes do Spring Boot e ajuste de propriedades sem reiniciar a aplicacao

## Stack

- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Security
- Spring Boot Actuator
- Spring Data JPA
- Spring Data Redis
- Spring Data MongoDB
- Spring Data Elasticsearch
- PostgreSQL
- Redis
- MongoDB
- Elasticsearch
- Maven
- Docker / Docker Compose

## Arquitetura resumida

### Persistencia relacional

- `audit`: schema legado apenas mapeado
- `aventura`: schema criado via `src/main/resources/db/schema-aventura.sql`

Entidades principais:

- `Aventureiro`
- `Companheiro`
- `Missao`
- `ParticipacaoMissao`

Regras de negocio principais:

- sem relacionamento cruzado entre organizacoes
- aventureiro inativo nao participa de novas missoes
- participacao unica por par `(missao, aventureiro)`
- companheiro em composicao `1:1` com o aventureiro
- missoes aceitam participantes apenas em estados validos

### Persistencia complementar

- Redis: cache do endpoint `GET /missoes/top15dias`
- MongoDB: colecao `historico_buscas_marketplace`
- Elasticsearch: consultas e agregacoes sobre o indice `guilda_loja`

## Seguranca

A aplicacao usa `HTTP Basic` com dois perfis padrao.

| Perfil | Usuario | Senha | Acesso |
| --- | --- | --- | --- |
| Admin | `estrategista` | `GuildaAdmin@123` | rotas administrativas e operacionais |
| Operador | `operador` | `GuildaOperador@123` | rotas operacionais |

### Rotas publicas

- `/`
- `/index.html`
- `/status`
- `/actuator/health`
- `/actuator/info`

### Rotas administrativas

- `/audit/**`
- `/diagnosticos/**`
- `/actuator/**`

### Rotas operacionais

- `/aventureiros/**`
- `/missoes/**`
- `/relatorios/**`
- `/produtos/**`

## Como executar

### Opcao recomendada: infra via Docker Compose + app via Maven

Subir a infra:

```bash
docker compose up -d guilda-db guilda-es guilda-redis guilda-mongo
```

Rodar a aplicacao:

```bash
mvn spring-boot:run
```

Aplicacao disponivel em:

- `http://localhost:8080/`
- `http://localhost:8080/status`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

### Opcao alternativa: stack completa em Docker

```bash
docker compose up -d --build
```

Essa opcao sobe a aplicacao no servico `guilda-app` junto com os servicos auxiliares.

### Parar os servicos

```bash
docker compose down
```

## Servicos da stack local

O arquivo [docker-compose.yml](docker-compose.yml) ja traz os containers usados no projeto:

- PostgreSQL: `guilda-db`
- Elasticsearch: `guilda-es`
- Redis: `guilda-redis`
- MongoDB: `guilda-mongo`
- aplicacao Spring Boot: `guilda-app`

Imagens principais:

- `leogloriainfnet/postgres-tp2-spring:2.0-win`
- `leogloriainfnet/elastic-tp2-spring:1.0-windows`
- `redis:7`
- `mongo:7`

## Configuracao

Os valores padrao ficam em [application.properties](src/main/resources/application.properties).

Principais propriedades:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/postgres}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.elasticsearch.uris=${ELASTICSEARCH_URI:http://localhost:9200}
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/guilda}
guilda.cache.ranking.ttl-segundos=${GUILDA_CACHE_RANKING_TTL_SEGUNDOS:300}
guilda.marketplace.historico.mongo.habilitado=${GUILDA_MARKETPLACE_HISTORICO_MONGO_HABILITADO:true}
guilda.security.admin.username=${GUILDA_SECURITY_ADMIN_USERNAME:estrategista}
guilda.security.admin.password=${GUILDA_SECURITY_ADMIN_PASSWORD:GuildaAdmin@123}
guilda.security.operador.username=${GUILDA_SECURITY_OPERADOR_USERNAME:operador}
guilda.security.operador.password=${GUILDA_SECURITY_OPERADOR_PASSWORD:GuildaOperador@123}
```

### Observacoes importantes sobre o banco

- o usuario `appuser` da imagem da avaliacao nao possui privilegio `REFERENCES` suficiente para criar as FKs do schema `aventura`
- por isso a aplicacao usa `postgres/postgres` por padrao
- o schema `aventura` e inicializado por script idempotente
- a view usada pelo painel tatico foi alinhada ao objeto real presente na imagem `2.0-win`

## Interface web

A pagina inicial em `http://localhost:8080/` funciona como painel publico para explorar os endpoints.

Ela permite:

- alternar entre requests publicos, operacionais e administrativos
- preencher credenciais de operador e admin
- testar os endpoints principais sem precisar montar as URLs manualmente
- visualizar exemplos de payloads e respostas

## Endpoints principais

### Audit

- `GET /audit/usuarios?organizacaoId=1`
- `GET /audit/usuarios/{id}`
- `POST /audit/usuarios`
- `GET /audit/roles?organizacaoId=1`
- `GET /audit/roles/{id}`

### Aventureiros

- `POST /aventureiros`
- `GET /aventureiros?organizacaoId=1&ativo=true&classe=MAGO&nivelMinimo=5&page=0&size=10&sortBy=nivel&direction=desc`
- `GET /aventureiros/busca?organizacaoId=1&nome=art&page=0&size=10`
- `GET /aventureiros/{id}?organizacaoId=1`
- `PUT /aventureiros/{id}?organizacaoId=1`
- `PATCH /aventureiros/{id}/encerrar-vinculo?organizacaoId=1`
- `PATCH /aventureiros/{id}/recrutar-novamente?organizacaoId=1`
- `PUT /aventureiros/{id}/companheiro?organizacaoId=1`
- `DELETE /aventureiros/{id}/companheiro?organizacaoId=1`

### Missoes

- `POST /missoes`
- `GET /missoes?organizacaoId=1&status=CONCLUIDA&nivelPerigo=ALTO&page=0&size=10`
- `GET /missoes/{id}?organizacaoId=1`
- `POST /missoes/{id}/participacoes?organizacaoId=1`
- `GET /missoes/top15dias`

### Relatorios

- `GET /relatorios/aventureiros/ranking?organizacaoId=1`
- `GET /relatorios/missoes?organizacaoId=1`

### Marketplace

- `GET /produtos/busca/nome?termo=espada`
- `GET /produtos/busca/descricao?termo=cura`
- `GET /produtos/busca/frase?termo=cura superior`
- `GET /produtos/busca/fuzzy?termo=espdaa`
- `GET /produtos/busca/multicampos?termo=dragao`
- `GET /produtos/busca/com-filtro?termo=pocao&categoria=pocoes`
- `GET /produtos/busca/faixa-preco?min=50&max=300`
- `GET /produtos/busca/avancada?categoria=armas&raridade=raro&min=200&max=1000`
- `GET /produtos/agregacoes/por-categoria`
- `GET /produtos/agregacoes/por-raridade`
- `GET /produtos/agregacoes/preco-medio`
- `GET /produtos/agregacoes/faixas-preco`
- `GET /produtos/buscas/historico`

### Diagnosticos

- `GET /diagnosticos/autoconfiguracao/persistencia`
- `GET /diagnosticos/configuracoes`
- `PATCH /diagnosticos/configuracoes?ttlRankingSegundos=120&historicoMongoHabilitado=false`

## Visualizacao do Elasticsearch

O Elasticsearch fica exposto na porta `9200`.

Consultas uteis:

- cluster: `http://localhost:9200`
- indices: `http://localhost:9200/_cat/indices?v`
- mapping do indice: `http://localhost:9200/guilda_loja/_mapping?pretty`
- busca simples: `http://localhost:9200/guilda_loja/_search?pretty`

## Testes

Executar a suite:

```bash
mvn test
```

Principais classes de teste:

- `ApplicationSecurityIntegrationTest`
- `AuditMappingRepositoryTest`
- `AventureiroQueryServiceTest`
- `MissaoRelatorioServiceTest`
- `PainelTaticoMissaoServiceTest`
- `PainelTaticoMissaoRedisCacheTest`
- `HistoricoBuscaProdutoServiceTest`
- `ConfiguracaoDinamicaServiceTest`

Cobertura validada em:

- seguranca e autorizacao por perfil
- mapeamentos do schema `audit`
- filtros e consultas de aventureiros
- regras e consultas de missoes
- ranking de participacao e metricas agregadas
- cache Redis do painel tatico
- historico Mongo do marketplace
- configuracao dinamica em runtime

### Observacao sobre os testes JPA

Os testes `@DataJpaTest` usam o PostgreSQL local do ambiente de execucao. Por isso, antes de rodar `mvn test`, o servico `guilda-db` precisa estar disponivel.

## Estrutura do projeto

```text
src/main/java/br/com/guilda/registro
  |- audit/
  |  |- domain/
  |  |- repository/
  |- config/
  |- controller/
  |- domain/
  |- dto/
  |- exception/
  |- mongodb/
  |  |- domain/
  |  |- repository/
  |- repository/
  |- service/
  |- validation/
src/main/resources
  |- db/schema-aventura.sql
  |- static/index.html
  |- static/app.js
src/test/java/br/com/guilda/registro
docs/
docker-compose.yml
Dockerfile
```

## Documentacao complementar

Arquivos incluidos no repositorio:

- [analise-requisitos.md](docs/analise-requisitos.md)
- [relatorio-rubricas.md](docs/relatorio-rubricas.md)

## Problemas comuns

### Porta 8080 ocupada

Se ja existir outra instancia da aplicacao em execucao:

```bash
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### Falha de autenticacao no PostgreSQL

Se a senha do container nao bater com a esperada, normalmente ha um volume antigo reaproveitado. Nessa situacao, recriar a infra com volumes novos costuma resolver:

```bash
docker compose down -v
docker compose up -d guilda-db guilda-es guilda-redis guilda-mongo
```

### JSON no PowerShell com `curl.exe`

No Windows, requisicoes `POST`, `PUT` e `PATCH` com JSON inline podem ser mais simples no Insomnia ou Postman. Se for usar `curl.exe`, uma alternativa segura e enviar o body a partir de arquivo com `--data-binary @arquivo.json`.
