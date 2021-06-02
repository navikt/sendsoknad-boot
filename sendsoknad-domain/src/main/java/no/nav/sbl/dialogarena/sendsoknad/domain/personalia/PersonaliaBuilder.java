package no.nav.sbl.dialogarena.sendsoknad.domain.personalia;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import org.joda.time.LocalDate;

public class PersonaliaBuilder {
    private String fnr;
    private LocalDate fodselsdato;
    private String alder;
    private String navn;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String mobiltelefonnummer;
    private String epost;
    private String diskresjonskode;
    private String statsborgerskap;
    private String kjonn;
    private Adresse gjeldendeAdresse;
    private Adresse sekundarAdresse;
    private Adresse folkeregistrertAdresse;
    private String kontonummer;
    private Boolean erUtenlandskBankkonto;
    private String utenlandskKontoBanknavn;
    private String utenlandskKontoLand;

    public static PersonaliaBuilder with() {
        return new PersonaliaBuilder();
    }

    public PersonaliaBuilder fodselsnummer(String fnr) {
        this.fnr = fnr;
        return this;
    }

    public PersonaliaBuilder fodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
        return this;
    }

    public PersonaliaBuilder diskresjonskode(String diskresjonskode) {
        this.diskresjonskode = diskresjonskode;
        return this;
    }

    public PersonaliaBuilder alder(String alder) {
        this.alder = alder;
        return this;
    }

    public PersonaliaBuilder navn(String navn) {
        this.navn = navn;
        return this;
    }

    public PersonaliaBuilder withFornavn(String fornavn) {
        this.fornavn = fornavn;
        return this;
    }

    public PersonaliaBuilder withMellomnavn(String mellomnavn) {
        this.mellomnavn = mellomnavn;
        return this;
    }

    public PersonaliaBuilder withEtternavn(String etternavn) {
        this.etternavn = etternavn;
        return this;
    }


    public PersonaliaBuilder mobiltelefon(String mobiltelefonnummer) {
        this.mobiltelefonnummer = mobiltelefonnummer;
        return this;
    }
    
    public PersonaliaBuilder epost(String epost) {
        this.epost = epost;
        return this;
    }

    public PersonaliaBuilder statsborgerskap(String statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
        return this;
    }

    public PersonaliaBuilder kjonn(String kjonn) {
        this.kjonn = kjonn;
        return this;
    }

    public PersonaliaBuilder gjeldendeAdresse(Adresse gjeldenseAdresse) {
        this.gjeldendeAdresse = gjeldenseAdresse;
        return this;
    }

    public PersonaliaBuilder sekundarAdresse(Adresse sekundarAdresse) {
        this.sekundarAdresse = sekundarAdresse;
        return this;
    }

    public PersonaliaBuilder kontonummer(String kontonummer) {
        this.kontonummer = kontonummer;
        return this;
    }

    public PersonaliaBuilder erUtenlandskBankkonto(Boolean erUtenlandskBankkonto) {
        this.erUtenlandskBankkonto = erUtenlandskBankkonto;
        return this;
    }

    public PersonaliaBuilder utenlandskKontoBanknavn(String utenlandskKontoBanknavn) {
        this.utenlandskKontoBanknavn = utenlandskKontoBanknavn;
        return this;
    }

    public PersonaliaBuilder utenlandskKontoLand(String utenlandskKontoLand) {
        this.utenlandskKontoLand = utenlandskKontoLand;
        return this;
    }
    
    public PersonaliaBuilder folkeregistrertAdresse(Adresse folkeregistrertAdresse) {
        this.folkeregistrertAdresse = folkeregistrertAdresse;
        return this;
    }

    public Personalia build() {
        Personalia personalia = new Personalia();

        personalia.setFnr(fnr);
        personalia.setFodselsdato(fodselsdato);
        personalia.setNavn(navn);
        personalia.setFornavn(fornavn);
        personalia.setMellomnavn(mellomnavn);
        personalia.setEtternavn(etternavn);
        personalia.setMobiltelefonnummer(mobiltelefonnummer);
        personalia.setEpost(epost);
        personalia.setStatsborgerskap(statsborgerskap);
        personalia.setKjonn(kjonn);
        personalia.setGjeldendeAdresse(gjeldendeAdresse);
        personalia.setSekundarAdresse(sekundarAdresse);
        personalia.setFolkeregistrertAdresse(folkeregistrertAdresse);
        personalia.setDiskresjonskode(diskresjonskode);
        personalia.setAlder(alder);
        personalia.setKontonummer(kontonummer);
        personalia.setErUtenlandskBankkonto(erUtenlandskBankkonto);
        personalia.setUtenlandskKontoBanknavn(utenlandskKontoBanknavn);
        personalia.setUtenlandskKontoLand(utenlandskKontoLand);

        return personalia;
    }
}
