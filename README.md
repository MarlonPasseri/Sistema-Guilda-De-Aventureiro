# Registro Oficial da Guilda de Aventureiros

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![API](https://img.shields.io/badge/API-REST-005571?logo=fastapi&logoColor=white)
![Persistencia](https://img.shields.io/badge/Persistencia-ArrayList_em_memoria-b8860b)
![Status](https://img.shields.io/badge/Status-Pronto_para_avaliacao-1f7a45)

API REST em Java para o **Registro Oficial da Guilda**, com foco em consistencia de regras de negocio, sem uso de banco de dados externo.

## Objetivo

Gerenciar aventureiros da guilda garantindo:

- registro valido com `id` gerado pelo sistema
- controle de estado (`ativo`/`inativo`) sem exclusao historica
- validacao rigorosa de `classe`, `nivel` e dados de `companheiro`
- composicao correta: companheiro existe apenas dentro de um aventureiro

## Tecnologias

- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Validation
- Maven

## Como executar

### 1) Requisitos

- Java 17+ instalado
- Maven instalado

### 2) Rodar localmente

```bash
mvn spring-boot:run
```

API disponivel em:

- `http://localhost:8080/` (UI amigavel)
- `http://localhost:8080/status` (status tecnico JSON)

### 3) Build

```bash
mvn clean package
```

### 4) Executar JAR

```bash
java -jar target/registro-guilda-0.0.1-SNAPSHOT.jar
```

## Regras de negocio implementadas

### Aventureiro

- `id` gerado automaticamente
- `nome` obrigatorio e nao vazio
- `classe` obrigatoria e restrita a:
  - `GUERREIRO`
  - `MAGO`
  - `ARQUEIRO`
  - `CLERIGO`
  - `LADINO`
- `nivel` obrigatorio e `>= 1`
- novo aventureiro sempre inicia com `ativo = true`
- aventureiro inativo continua registrado

### Companheiro (composicao)

- opcional no aventureiro
- nao existe isoladamente
- nao e compartilhado
- quando informado:
  - `nome` obrigatorio
  - `especie` obrigatoria e restrita a:
    - `LOBO`
    - `CORUJA`
    - `GOLEM`
    - `DRAGAO_MINIATURA`
  - `lealdade` entre `0` e `100`

### Validacoes de requisicao

- payloads invalidos retornam `400 Bad Request`
- recurso inexistente retorna `404 Not Found`
- erros seguem formato padrao:

```json
{
  "mensagem": "Solicitacao invalida",
  "detalhes": [
    "classe invalida",
    "nivel deve ser maior ou igual a 1"
  ]
}
```

## Endpoints

Base URL: `http://localhost:8080`

### 1) Registrar aventureiro

- **POST** `/aventureiros`
- Body:

```json
{
  "nome": "Arthos",
  "classe": "GUERREIRO",
  "nivel": 7
}
```

- Retorno: `201 Created` + header `Location`

### 2) Listar aventureiros (filtros + paginacao)

- **GET** `/aventureiros`
- Query params opcionais:
  - `classe`
  - `ativo`
  - `nivelMinimo`
  - `page` (default: `0`, minimo: `0`)
  - `size` (default: `10`, intervalo: `1..50`)

Exemplo:

`GET /aventureiros?classe=MAGO&ativo=true&nivelMinimo=5&page=0&size=10`

Headers de resposta obrigatorios:

- `X-Total-Count`
- `X-Page`
- `X-Size`
- `X-Total-Pages`

Observacoes:

- ordenacao sempre crescente por `id`
- filtros aplicados antes da paginacao
- pagina fora do intervalo retorna lista vazia + headers corretos

### 3) Consultar por ID

- **GET** `/aventureiros/{id}`
- Retorna dados completos, incluindo companheiro quando houver

### 4) Atualizar aventureiro

- **PUT** `/aventureiros/{id}`
- Permite atualizar somente:
  - `nome`
  - `classe`
  - `nivel`

Nao permite alterar:

- `id`
- `ativo`
- `companheiro`

### 5) Encerrar vinculo

- **PATCH** `/aventureiros/{id}/encerrar-vinculo`
- Seta `ativo = false`

### 6) Recrutar novamente

- **PATCH** `/aventureiros/{id}/recrutar-novamente`
- Seta `ativo = true`

### 7) Definir/substituir companheiro

- **PUT** `/aventureiros/{id}/companheiro`
- Body:

```json
{
  "nome": "Fenrir",
  "especie": "LOBO",
  "lealdade": 95
}
```

### 8) Remover companheiro

- **DELETE** `/aventureiros/{id}/companheiro`
- Retorno: `204 No Content`

## Estrutura do projeto

```text
src/main/java/br/com/guilda/registro
  ├─ controller
  ├─ service
  ├─ repository
  ├─ domain
  ├─ dto
  ├─ exception
  └─ validation
```

## Persistencia em memoria

- Implementada por classe repositório com `ArrayList`
- Inicializada com **120 registros** ao subir a aplicacao
- Sem banco externo, conforme requisito

## Interface amigavel

A raiz `/` entrega uma pagina HTML com:

- visual de painel da guilda
- mapa de endpoints
- console interativo para testar:
  - `GET /status`
  - `GET /aventureiros?page=0&size=10`
  - `GET /aventureiros/{id}`

## Autor

- Projeto desenvolvido para o TP1 Java 2026.
