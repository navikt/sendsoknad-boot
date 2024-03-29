CREATE TABLE HENDELSE(
    BEHANDLINGSID VARCHAR2(255 CHAR) NOT NULL,
    HENDELSE_TYPE VARCHAR2(255 CHAR) NOT NULL,
    HENDELSE_TIDSPUNKT TIMESTAMP(6) DEFAULT SYSDATE,
    VERSJON NUMBER,
    SKJEMANUMMER VARCHAR2(255 CHAR),
    SIST_HENDELSE NUMBER(1,0) NOT NULL
);

CREATE INDEX INDEX_HENDELSE_BEHID on HENDELSE(BEHANDLINGSID);
CREATE INDEX INDEX_HENDELSE_SIST_HENDELSE on HENDELSE(SIST_HENDELSE);