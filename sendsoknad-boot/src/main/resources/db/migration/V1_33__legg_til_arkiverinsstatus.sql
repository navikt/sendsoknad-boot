ALTER TABLE soknad ADD COLUMN IF NOT EXISTS arkiveringsstatus VARCHAR(20) NOT NULL default 'IkkeSatt';

UPDATE soknad SET arkiveringsstatus='Arkivert' WHERE status = 'FERDIG';

CREATE INDEX IF NOT EXISTS soknad_arkiveringsstatus_idx ON soknad(arkiveringsstatus);
