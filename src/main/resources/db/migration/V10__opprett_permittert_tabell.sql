CREATE TABLE permittert (
    id SERIAL PRIMARY KEY,
    aktor_id VARCHAR(20),
    opprettet TIMESTAMP(6) NOT NULL DEFAULT current_timestamp,
    slettet BOOLEAN,
    status_fra_veilarb VARCHAR(100),
    tidspunkt_for_status_fra_veilarb TIMESTAMP(6)
);
