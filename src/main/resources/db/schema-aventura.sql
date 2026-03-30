CREATE SCHEMA IF NOT EXISTS aventura;

CREATE TABLE IF NOT EXISTS aventura.aventureiros (
  id                  BIGSERIAL PRIMARY KEY,
  organizacao_id      BIGINT NOT NULL,
  usuario_cadastro_id BIGINT NOT NULL,
  nome                VARCHAR(120) NOT NULL,
  classe              VARCHAR(30) NOT NULL,
  nivel               INTEGER NOT NULL,
  ativo               BOOLEAN NOT NULL DEFAULT TRUE,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_aventureiros_org
    FOREIGN KEY (organizacao_id) REFERENCES audit.organizacoes(id),
  CONSTRAINT fk_aventureiros_usuario
    FOREIGN KEY (usuario_cadastro_id) REFERENCES audit.usuarios(id),
  CONSTRAINT ck_aventureiros_classe
    CHECK (classe IN ('GUERREIRO', 'MAGO', 'ARQUEIRO', 'CLERIGO', 'LADINO')),
  CONSTRAINT ck_aventureiros_nivel
    CHECK (nivel >= 1)
);

CREATE INDEX IF NOT EXISTS idx_aventureiros_org ON aventura.aventureiros(organizacao_id);
CREATE INDEX IF NOT EXISTS idx_aventureiros_nome ON aventura.aventureiros(nome);
CREATE INDEX IF NOT EXISTS idx_aventureiros_classe ON aventura.aventureiros(classe);
CREATE INDEX IF NOT EXISTS idx_aventureiros_ativo ON aventura.aventureiros(ativo);

CREATE TABLE IF NOT EXISTS aventura.companheiros (
  aventureiro_id    BIGINT PRIMARY KEY,
  nome              VARCHAR(120) NOT NULL,
  especie           VARCHAR(30) NOT NULL,
  indice_lealdade   INTEGER NOT NULL,

  CONSTRAINT fk_companheiros_aventureiro
    FOREIGN KEY (aventureiro_id) REFERENCES aventura.aventureiros(id) ON DELETE CASCADE,
  CONSTRAINT ck_companheiros_especie
    CHECK (especie IN ('LOBO', 'CORUJA', 'GOLEM', 'DRAGAO_MINIATURA')),
  CONSTRAINT ck_companheiros_lealdade
    CHECK (indice_lealdade BETWEEN 0 AND 100)
);

CREATE TABLE IF NOT EXISTS aventura.missoes (
  id             BIGSERIAL PRIMARY KEY,
  organizacao_id BIGINT NOT NULL,
  titulo         VARCHAR(150) NOT NULL,
  nivel_perigo   VARCHAR(30) NOT NULL,
  status         VARCHAR(30) NOT NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  data_inicio    TIMESTAMPTZ,
  data_termino   TIMESTAMPTZ,

  CONSTRAINT fk_missoes_org
    FOREIGN KEY (organizacao_id) REFERENCES audit.organizacoes(id),
  CONSTRAINT ck_missoes_nivel_perigo
    CHECK (nivel_perigo IN ('BAIXO', 'MEDIO', 'ALTO', 'EPICO')),
  CONSTRAINT ck_missoes_status
    CHECK (status IN ('PLANEJADA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX IF NOT EXISTS idx_missoes_org ON aventura.missoes(organizacao_id);
CREATE INDEX IF NOT EXISTS idx_missoes_status ON aventura.missoes(status);
CREATE INDEX IF NOT EXISTS idx_missoes_nivel_perigo ON aventura.missoes(nivel_perigo);
CREATE INDEX IF NOT EXISTS idx_missoes_created_at ON aventura.missoes(created_at);

CREATE TABLE IF NOT EXISTS aventura.participacoes_missao (
  missao_id          BIGINT NOT NULL,
  aventureiro_id     BIGINT NOT NULL,
  papel_na_missao    VARCHAR(30) NOT NULL,
  recompensa_ouro    NUMERIC(12, 2) NOT NULL DEFAULT 0,
  destaque           BOOLEAN NOT NULL DEFAULT FALSE,
  data_registro      TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (missao_id, aventureiro_id),

  CONSTRAINT fk_participacoes_missao
    FOREIGN KEY (missao_id) REFERENCES aventura.missoes(id) ON DELETE CASCADE,
  CONSTRAINT fk_participacoes_aventureiro
    FOREIGN KEY (aventureiro_id) REFERENCES aventura.aventureiros(id) ON DELETE CASCADE,
  CONSTRAINT ck_participacoes_papel
    CHECK (papel_na_missao IN ('LIDER', 'CURANDEIRO', 'DANO', 'SUPORTE', 'EXPLORADOR')),
  CONSTRAINT ck_participacoes_recompensa
    CHECK (recompensa_ouro >= 0)
);

CREATE INDEX IF NOT EXISTS idx_participacoes_aventureiro
  ON aventura.participacoes_missao(aventureiro_id);
