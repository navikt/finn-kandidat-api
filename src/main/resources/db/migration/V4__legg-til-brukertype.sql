ALTER TABLE kandidat ADD COLUMN registrert_av_brukertype TEXT;
UPDATE kandidat SET registrert_av_brukertype='VEILEDER';
