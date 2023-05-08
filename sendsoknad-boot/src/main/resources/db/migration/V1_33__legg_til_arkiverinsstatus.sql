ALTER TABLE soknad ADD COLUMN IF NOT EXISTS arkiveringsstatus VARCHAR(20) NOT NULL default 'IkkeSatt';
