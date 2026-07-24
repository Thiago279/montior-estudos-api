CREATE TABLE sessao_estudo (
    id BIGSERIAL PRIMARY KEY,
    data_inicio TIMESTAMP(6) NOT NULL,
    data_fim TIMESTAMP(6),
    materia_id BIGINT NOT NULL,

    CONSTRAINT fk_sessao_estudo_materia
      FOREIGN KEY (materia_id)
      REFERENCES materia (id)
);
