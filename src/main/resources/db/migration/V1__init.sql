CREATE TABLE tilretteleggingsbehov (
    id SERIAL PRIMARY KEY,
    opprettet TIMESTAMP(6) DEFAULT current_timestamp NOT NULL,
    opprettet_av_ident VARCHAR(7) NOT NULL,
    bruker_fnr VARCHAR(11),
    arbeidstid VARCHAR(255),
    fysisk VARCHAR(1000)
--     TODO:
--     arbeidsmiljo VARCHAR(255),
--     grunnleggende VARCHAR(255)
);

