ALTER TABLE soknad ADD COLUMN arkiveringsstatus varchar(20 CHAR) NOT NULL default 'IkkeSatt';

UPDATE soknad SET arkiveringsstatus='Arkivert' WHERE status = 'FERDIG';

CREATE INDEX soknad_arkiveringsstatus_idx ON soknad(arkiveringsstatus);
