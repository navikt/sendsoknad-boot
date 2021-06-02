package no.nav.sbl.dialogarena.sendsoknad.mockmodul.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;

public class PersonMock {
    private static PersonMock personMock = new PersonMock();
    private PersonPortTypeMock personPortTypeMock;

    private PersonMock() {
        String fnr0 = "12312312345"; // Ikke ekte person
        String fnr1 = "01010081336"; // Ikke ekte person
        String fnr2 = "01018012345"; // Ikke ekte person

        personPortTypeMock = new PersonPortTypeMock();

        // Endre også i override-web.xml sin defaultFnr, da det er den som ligger på getSubjectHandler().getUid()
        Person person = genererPersonMedGyldigIdentOgNavn(fnr0, "person", "mock");
        person.setFoedselsdato(fodseldato(1963, 7, 3));

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkoder = new Landkoder();
        landkoder.setValue("NOR");
        statsborgerskap.setLand(landkoder);
        person.setStatsborgerskap(statsborgerskap);

        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();

        Doedsdato doedsdato = new Doedsdato();
        doedsdato.setDoedsdato(ServiceUtils.stringTilXmldato("2014-02-02"));
        familieRelasjoner.add(lagBarn(fnr0, "Dole", "Mockmann", doedsdato));
        familieRelasjoner.add(lagBarn(fnr1, "Ole", "Mockmann"));
        familieRelasjoner.add(lagBarn(fnr2, "Doffen", "Mockmann"));

        personPortTypeMock.setPerson(person);
    }

    public static PersonMock getInstance() {
        return personMock;
    }

    public PersonPortTypeMock getPersonPortTypeMock() {
        return personPortTypeMock;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn) {
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn = genererPersonMedGyldigIdentOgNavn(fnr, fornavn, etternavn);
        familierelasjon.setTilPerson(barn);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();
        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);
        return familierelasjon;
    }

    private Familierelasjon lagBarn(String fnr, String fornavn, String etternavn, Doedsdato doedsdato) {
        Familierelasjon familierelasjon = lagBarn(fnr, fornavn, etternavn);
        familierelasjon.getTilPerson().setDoedsdato(doedsdato);
        return familierelasjon;
    }

    private Person genererPersonMedGyldigIdentOgNavn(String ident, String fornavn, String etternavn) {
        Person xmlPerson = new Person();

        Personnavn personnavn = new Personnavn();
        personnavn.setFornavn(fornavn);
        personnavn.setMellomnavn("");
        personnavn.setEtternavn(etternavn);
        xmlPerson.setPersonnavn(personnavn);

        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(ident);
        xmlPerson.setIdent(norskIdent);

        return xmlPerson;
    }

    private Foedselsdato fodseldato(int year, int month, int day) {
        Foedselsdato foedselsdato = new Foedselsdato();
        foedselsdato.setFoedselsdato(lagDatatypeFactory().newXMLGregorianCalendarDate(year, month, day, 0));
        return foedselsdato;
    }

    private DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
