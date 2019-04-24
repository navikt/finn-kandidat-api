CREATE TABLE tilretteleggingsbehov (
    id SERIAL PRIMARY KEY,
    opprettet TIMESTAMP(6) DEFAULT current_timestamp,
    opprettet_av_ident VARCHAR(7),
    bruker_fnr VARCHAR(11),
    arbeidstid VARCHAR(255),
    fysisk VARCHAR(1000),
    arbeidsmiljo VARCHAR(1000),
    grunnleggende VARCHAR(1000)
);

