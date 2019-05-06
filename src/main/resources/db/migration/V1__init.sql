CREATE TABLE kandidat (
    id SERIAL PRIMARY KEY,
    fnr VARCHAR(11),
    registrert_av VARCHAR(7),
    registreringstidspunkt TIMESTAMP(6) DEFAULT current_timestamp,
    arbeidstid_behov TEXT,
    fysiske_behov TEXT,
    arbeidsmiljø_behov TEXT,
    grunnleggende_behov TEXT
);
