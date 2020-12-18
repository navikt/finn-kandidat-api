CREATE TABLE samtykke (
                          id                  SERIAL PRIMARY KEY,
                          foedselsnummer      varchar(11),
                          gjelder             VARCHAR(255),
                          endring             VARCHAR(255),
                          opprettet_tidspunkt TIMESTAMP
)