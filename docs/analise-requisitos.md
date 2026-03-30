# Analise de Requisitos

## Parte 2 - Expansao do Dominio

### Separacao de schemas

- `audit`: nucleo legado, apenas mapeado
- `aventura`: novo dominio, criado pela aplicacao via `schema-aventura.sql`

### Entidades persistentes

#### Aventureiro

- identificador `BIGSERIAL`
- referencia obrigatoria a `audit.organizacoes`
- referencia obrigatoria a `audit.usuarios`
- nome ate 120 caracteres
- classe em enum fixo da aplicacao
- nivel minimo 1
- ativo obrigatorio
- `created_at` e `updated_at`

#### Companheiro

- relacao `1:1` com `Aventureiro`
- chave primaria compartilhada com o aventureiro
- nome ate 120 caracteres
- especie em enum fixo
- indice de lealdade entre 0 e 100
- exclusao por composicao

#### Missao

- identificador `BIGSERIAL`
- referencia obrigatoria a `audit.organizacoes`
- titulo ate 150 caracteres
- nivel de perigo em enum fixo
- status em enum fixo
- `created_at`
- `data_inicio` e `data_termino` opcionais

#### ParticipacaoMissao

- chave composta `(missao_id, aventureiro_id)`
- papel na missao em enum fixo
- recompensa em ouro maior ou igual a zero
- destaque obrigatorio
- `data_registro`

### Regras de integridade implementadas

- um aventureiro pertence a uma unica organizacao
- uma missao pertence a uma unica organizacao
- participacao so pode ocorrer entre elementos da mesma organizacao
- aventureiro inativo nao entra em novas missoes
- missao so aceita participante em `PLANEJADA` ou `EM_ANDAMENTO`
- companheiro nao existe sem aventureiro
- participacao nao se repete para o mesmo par missao-aventureiro

### Integridade estrutural

- FKs do schema `aventura` apontam para o legado `audit`
- checks SQL garantem dominio de enums e limites numericos
- remocao de dependencias do companheiro e das participacoes protegida por chave e `ON DELETE CASCADE` no banco

## Parte 3 - Consultas e Relatorios

### Consultas operacionais

#### Aventureiros

- listagem com filtros por:
  - status
  - classe
  - nivel minimo
- busca parcial por nome
- ordenacao por nome ou nivel
- paginacao
- visualizacao completa com:
  - dados principais
  - companheiro
  - total de participacoes
  - ultima missao registrada

#### Missoes

- listagem com filtros por:
  - status
  - nivel de perigo
  - intervalo por `created_at`
- detalhamento com participantes
- exibicao de papel, recompensa e destaque

### Relatorios

#### Ranking de participacao

Apresenta por aventureiro:

- total de participacoes
- soma de recompensas
- quantidade de destaques

Filtros:

- periodo
- status de missao opcional

#### Relatorio de missoes

Apresenta por missao:

- titulo
- status
- nivel de perigo
- quantidade de participantes
- total de recompensas distribuidas

### Criterios tecnicos observados

- sem duplicidade indevida nos agregados
- consultas paginadas usando Spring Data JPA
- relacionamentos carregados com `EntityGraph` quando necessario
- testes cobrindo cada busca/consulta principal

## Observacao de infraestrutura

A imagem da avaliacao cria o usuario `appuser`, mas ele nao recebe privilegio `REFERENCES` nas tabelas do schema `audit`. Como o novo dominio `aventura` precisa criar FKs apontando para o legado, a aplicacao foi configurada por padrao para iniciar com o usuario `postgres`.
