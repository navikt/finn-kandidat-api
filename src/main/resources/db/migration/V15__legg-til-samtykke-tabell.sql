CREATE TABLE samtykke
(
    id                  SERIAL PRIMARY KEY,
    foedselsnummer      VARCHAR(11),
    gjelder             VARCHAR(255),
    endring             VARCHAR(255),
    opprettet_tidspunkt TIMESTAMP,
    aktor_id            VARCHAR(13)
);

ALTER TABLE samtykke
    ADD CONSTRAINT unique_aktor_id_gjelder UNIQUE (aktor_id, gjelder);
