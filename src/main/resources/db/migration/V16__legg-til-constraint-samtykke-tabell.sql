ALTER TABLE samtykke
    ADD CONSTRAINT unique_aktor_id_gjelder UNIQUE (aktor_id, gjelder);