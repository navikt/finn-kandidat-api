CREATE TABLE tilretteleggingsbehov (
    id SERIAL PRIMARY KEY,
    opprettet TIMESTAMP(6) DEFAULT current_timestamp NOT NULL,
    opprettet_av_ident VARCHAR(7) NOT NULL,
    bruker_fnr VARCHAR(11),
    arbeidstid VARCHAR(255),
--     TODO: Disse er flervalg, må være separat table med foreign keys
--     fysisk VARCHAR(255),
--     arbeidsmiljo VARCHAR(255),
--     grunnleggende VARCHAR(255)
);
