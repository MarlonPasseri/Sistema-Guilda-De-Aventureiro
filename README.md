# Sistema de Gestao de Aventureiros

Projeto Spring Boot evoluido para trabalhar com:

- schema legado `audit` ja existente no PostgreSQL
- novo schema `aventura` criado pela aplicacao
- consultas operacionais e relatorios do dominio de aventureiros

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

## Stack

- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven

## Banco da avaliacao

Imagem utilizada:

- `leogloriainfnet/postgres-tp2-spring:1.0`

Observacao importante:

- o usuario `appuser` criado pela imagem nao possui privilegio `REFERENCES` suficiente para criar as FKs do schema `aventura`
- por isso a aplicacao esta configurada por padrao para subir com `postgres/postgres`
- se quiser usar outras credenciais, basta sobrescrever `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`

## Como executar

### 1. Subir o PostgreSQL legado

```bash
docker run -d --name tp1-audit-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 leogloriainfnet/postgres-tp2-spring:1.0
```

### 2. Rodar a aplicacao

```bash
mvn spring-boot:run
```

API disponivel em:

- `http://localhost:8080/`
- `http://localhost:8080/status`

### 3. Rodar os testes

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

## Documento de analise

Foi adicionado:

- `docs/analise-requisitos.md`

Nao gerei PDF automaticamente porque o ambiente atual nao possui ferramenta instalada para conversao direta.
