ALTER TABLE kandidat ADD COLUMN opprettet TIMESTAMP(6) DEFAULT current_timestamp;
UPDATE kandidat SET opprettet = registreringstidspunkt;
ALTER TABLE kandidat ALTER COLUMN opprettet SET NOT NULL;