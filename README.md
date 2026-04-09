# Sistema de Gestao de Aventureiros

Projeto Spring Boot evoluido para trabalhar com:

- schema legado `audit` ja existente no PostgreSQL
- novo schema `aventura` criado pela aplicacao
- consultas operacionais e relatorios do dominio de aventureiros
- ranking tatico de missoes com cache na aplicacao
- buscas e agregacoes no Elasticsearch para o marketplace da guilda
- historico de buscas persistido em MongoDB
- cache do ranking tatico persistido em Redis via `RedisTemplate`
- diagnostico de persistencia e configuracao dinamica em tempo de execucao

## O que foi implementado

### Parte 1

- entidades JPA para todo o schema `audit`
- mapeamento de `@ManyToOne`, `@OneToMany` e `@ManyToMany`
- tabelas de juncao com chave composta:
  - `audit.user_roles`
  - `audit.role_permissions`
- repositorios para organizacoes, usuarios, roles, permissions, api keys e audit entries
- endpoints para:
  - listar usuarios com roles
  - listar roles com permissions
  - criar usuario vinculado a organizacao existente

### Parte 2

- schema `aventura` criado via script idempotente
- entidades persistentes:
  - `Aventureiro`
  - `Companheiro`
  - `Missao`
  - `ParticipacaoMissao`
- integridade entre organizacoes, usuarios, aventureiros e missoes
- regras de negocio principais:
  - sem relacionamento cruzado entre organizacoes
  - aventureiro inativo nao entra em novas missoes
  - participacao unica por par `(missao, aventureiro)`
  - companheiro com composicao `1:1` e remocao junto do aventureiro

### Parte 3

- listagem de aventureiros com filtros, ordenacao e paginacao
- busca textual parcial por nome
- perfil completo do aventureiro com companheiro, total de participacoes e ultima missao
- listagem de missoes com filtros, ordenacao e paginacao
- detalhamento de missao com participantes
- ranking de participacao
- relatorio de missoes com metricas agregadas
- testes para cada busca/consulta relevante

### Parte 4

- endpoint `GET /missoes/top15dias`
- leitura do painel tatico de missoes no schema `operacoes`
- filtro de ultimos 15 dias aplicado na camada de service
- ordenacao por `indice_prontidao` desc e limite de 10 registros
- cache Redis com expiracao configuravel para reduzir consultas repetidas

### Parte 5

- integracao da aplicacao com o indice Elasticsearch `guilda_loja`
- buscas textuais por `nome`, `descricao`, frase exata, fuzzy e multicampos
- filtros por `categoria`, `raridade` e faixa de preco
- agregacoes por categoria, raridade, preco medio e faixas de preco
- historico de buscas do marketplace salvo em MongoDB via `MongoRepository`
- consulta desse historico por endpoint REST

### Parte 6

- configuracao dinamica em runtime via `ConfigurableEnvironment`
- leitura do relatorio de autoconfiguracao do Spring Boot com `ConditionEvaluationReport`
- endpoint para inspecionar auto-configuracoes de JPA, Mongo e Redis
- endpoint para alterar em tempo de execucao o TTL do cache Redis e o toggle do historico Mongo

## Stack

- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Data JPA
- Spring Data Redis
- Spring Data MongoDB
- Spring Boot Actuator
- Elasticsearch
- PostgreSQL
- Maven

## Banco da avaliacao

Imagem utilizada:

- `leogloriainfnet/postgres-tp2-spring:2.0-win`
- `leogloriainfnet/elastic-tp2-spring:1.0-windows`

Observacao importante:

- o usuario `appuser` criado pela imagem nao possui privilegio `REFERENCES` suficiente para criar as FKs do schema `aventura`
- por isso a aplicacao esta configurada por padrao para subir com `postgres/postgres`
- se quiser usar outras credenciais, basta sobrescrever `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`
- na imagem `2.0-win`, o objeto existente no schema `operacoes` veio como `vw_painel_tatico_missao`; o mapeamento foi alinhado a esse objeto real da imagem para manter a aplicacao funcional no ambiente fornecido

## Como executar

### 1. Subir o PostgreSQL legado

```bash
docker run -d --name guilda-tp2-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 leogloriainfnet/postgres-tp2-spring:2.0-win
```

### 2. Subir o Elasticsearch

```bash
docker run -d --name guilda-es -p 9200:9200 -e ES_JAVA_OPTS="-Xms512m -Xmx512m" leogloriainfnet/elastic-tp2-spring:1.0-windows
```

### 3. Subir o Redis

```bash
docker run -d --name guilda-redis -p 6379:6379 redis:7
```

### 4. Subir o MongoDB

```bash
docker run -d --name guilda-mongo -p 27017:27017 mongo:7
```

### 5. Rodar a aplicacao

```bash
mvn spring-boot:run
```

API disponivel em:

- `http://localhost:8080/`
- `http://localhost:8080/status`
- `http://localhost:8080/actuator/conditions`
- `http://localhost:8080/actuator/env`

### 6. Rodar os testes

```bash
mvn test
```

## Configuracao

Os valores padrao estao em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:db/schema-aventura.sql
spring.elasticsearch.uris=http://localhost:9200
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.mongodb.uri=mongodb://localhost:27017/guilda
guilda.cache.ranking.ttl-segundos=300
guilda.marketplace.historico.mongo.habilitado=true
```

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

### Relatorios

- `GET /relatorios/aventureiros/ranking?organizacaoId=1`
- `GET /relatorios/missoes?organizacaoId=1`

### Painel Tatico

- `GET /missoes/top15dias`

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

## Estrutura resumida

```text
src/main/java/br/com/guilda/registro
  |- audit/domain
  |- audit/repository
  |- controller
  |- domain
  |- dto
  |- exception
  |- repository
  |- service
  |- validation
src/main/resources/db/schema-aventura.sql
src/test/java/br/com/guilda/registro
```

## Testes implementados

- `AuditMappingRepositoryTest`
- `AventureiroQueryServiceTest`
- `MissaoRelatorioServiceTest`
- `PainelTaticoMissaoServiceTest`
- `PainelTaticoMissaoRedisCacheTest`
- `HistoricoBuscaProdutoServiceTest`
- `ConfiguracaoDinamicaServiceTest`

Cobertura validada com:

- carga de usuario com roles
- carga de role com permissions
- persistencia de novo usuario em organizacao existente
- filtros de aventureiros
- busca parcial por nome
- perfil completo do aventureiro
- filtros de missao
- detalhamento de missao
- ranking de participacao
- relatorio de missoes com metricas
- calculo da janela dos ultimos 15 dias
- reaproveitamento do cache Redis do ranking tatico
- persistencia do historico de buscas em MongoDB
- atualizacao dinamica das propriedades de runtime

## Estrategia de cache e persistencia complementar

Foi aplicada uma estrategia de cache em Redis na camada de service do ranking tatico:

- uso explicito de `StringRedisTemplate`
- chave `missoes:top15dias`
- TTL controlado pela propriedade `guilda.cache.ranking.ttl-segundos`
- objetivo: reduzir repeticao da consulta pesada sobre o painel tatico sem perder atualizacao recente dos dados

Historico NoSQL:

- uso de `MongoRepository` para a colecao `historico_buscas_marketplace`
- registro das consultas executadas no marketplace
- endpoint proprio para consulta do historico salvo

Configuracao dinamica:

- alteracao das propriedades em runtime via `ConfigurableEnvironment`
- diagnostico das auto-configuracoes com `ConditionEvaluationReport`
- suporte para ajustar TTL do cache e habilitar/desabilitar historico Mongo sem reiniciar a aplicacao

## Documento de analise

Foi adicionado:

- `docs/analise-requisitos.md`

