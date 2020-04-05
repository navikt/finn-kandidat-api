CREATE TABLE vedtak (
    id BIGSERIAL PRIMARY KEY,
    opprettet TIMESTAMP(6) NOT NULL DEFAULT current_timestamp,
    slettet BOOLEAN,
    aktor_id VARCHAR(20),
    fnr VARCHAR(11),
    vedtak_id BIGINT,
    sak_id BIGINT,
    person_id BIGINT,
    vedtaktypekode VARCHAR(2),
    vedtakstatuskode VARCHAR(10),
    utfallkode VARCHAR(4),
    rettighetkode VARCHAR(8),
    fra_dato TIMESTAMP(6),
    til_dato TIMESTAMP(6),
    ts_fra_arena TIMESTAMP(6),
    pos_fra_arena VARCHAR(100),
    op_fra_arena VARCHAR(1)
);

CREATE INDEX idx_vedtak_aktorid
ON vedtak(aktor_id);

CREATE INDEX idx_vedtak_kombo
ON vedtak(aktor_id, vedtak_id, id);