ALTER TABLE samtykke
    ADD COLUMN opprettet_tidspunkt TIMESTAMP;
ALTER TABLE samtykke DROP COLUMN endring;