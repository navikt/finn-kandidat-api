CREATE TABLE utilgjengelig (
    id SERIAL PRIMARY KEY,
    aktor_id VARCHAR(20),
    registrert_av VARCHAR(7),
    fra_dato TIMESTAMP(6),
    til_dato TIMESTAMP(6),
    registreringstidspunkt TIMESTAMP(6) DEFAULT current_timestamp,
    slettet BOOLEAN
);
