CREATE TABLE kandidat (
    id SERIAL PRIMARY KEY,
    fnr VARCHAR(11),
    registrert_av VARCHAR(7),
    registreringstidspunkt TIMESTAMP(6) DEFAULT current_timestamp,
    arbeidstid_behov VARCHAR(255),
    fysiske_behov VARCHAR(1000),
    arbeidsmiljø_behov VARCHAR(1000),
    grunnleggende_behov VARCHAR(1000),
    slettet BOOLEAN
);
