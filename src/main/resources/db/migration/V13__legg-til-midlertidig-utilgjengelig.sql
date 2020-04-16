CREATE TABLE midlertidig_utilgjengelig (
    id SERIAL PRIMARY KEY,
    aktor_id VARCHAR(20),
    fra_dato TIMESTAMP(6) DEFAULT current_timestamp,
    til_dato TIMESTAMP(6),
    registrert_av_ident VARCHAR(7),
    registrert_av_navn TEXT,
    sist_endret_tidspunkt TIMESTAMP(6) DEFAULT current_timestamp,
    sist_endret_av_ident VARCHAR(7),
    sist_endret_av_navn TEXT,
);
