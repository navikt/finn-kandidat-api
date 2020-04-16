CREATE TABLE midlertidig_utilgjengelig (
    id SERIAL PRIMARY KEY,
    aktor_id VARCHAR(20),
    registrert_av_ident VARCHAR(7),
    registrert_av_navn VARCHAR(100),
    fra_dato TIMESTAMP(6),
    til_dato TIMESTAMP(6),
    registreringstidspunkt TIMESTAMP(6) DEFAULT current_timestamp,
    slettet BOOLEAN
);
