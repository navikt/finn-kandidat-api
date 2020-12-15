CREATE TABLE samtykke (
    aktor_id VARCHAR(20),
    gjelder VARCHAR(255),
    endring VARCHAR(255),
    PRIMARY KEY(aktor_id, gjelder)
)