package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSEpostadresse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSMobiltelefonnummer;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.xml.ws.WebServiceException;
import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(value = MockitoJUnitRunner.class)
public class PersonaliaFletterTest {

    private static final String IDENT = "56128349974"; // Ikke ekteperson
    private static final String BARN_IDENT = "01018012345"; // Ikke ekteperson
    private static final String BARN_FORNAVN = "Bjarne";
    private static final String BARN_ETTERNAVN = "Barnet";

    private static final String ET_FORNAVN = "Ola";
    private static final String ET_MELLOMNAVN = "Johan";
    private static final String ET_ETTERNAVN = "Normann";
    private static final String FOLKEREGISTRERT_ADRESSE_VALUE = "BOSTEDSADRESSE";
    private static final String EN_ADRESSE_GATE = "Grepalida";
    private static final String EN_ADRESSE_HUSNUMMER = "44";
    private static final String EN_ADRESSE_HUSBOKSTAV = "B";
    private static final String EN_ADRESSE_POSTNUMMER = "0560";
    private static final String EN_ADRESSE_POSTSTED = "Oslo";

    private static final Long EN_ANNEN_ADRESSE_GYLDIG_FRA = new DateTime(2012, 10, 11, 14, 44).getMillis();
    private static final Long EN_ANNEN_ADRESSE_GYLDIG_TIL = new DateTime(2012, 11, 12, 15, 55).getMillis();
    private static final String EN_ANNEN_ADRESSE_GATE = "Vegvegen";
    private static final String EN_ANNEN_ADRESSE_HUSNUMMER = "44";
    private static final String EN_ANNEN_ADRESSE_HUSBOKSTAV = "D";
    private static final String EN_ANNEN_ADRESSE_POSTNUMMER = "0565";

    private static final String EN_POSTBOKS_ADRESSEEIER = "Per Conradi";
    private static final String ET_POSTBOKS_NAVN = "Postboksstativet";
    private static final String EN_POSTBOKS_NUMMER = "66";

    private static final String EN_ADRESSELINJE = "Poitigatan 55";
    private static final String EN_ANNEN_ADRESSELINJE = "Nord-Poiti";
    private static final String EN_TREDJE_ADRESSELINJE = "1111";
    private static final String EN_FJERDE_ADRESSELINJE = "Helsinki";
    private static final String ET_LAND = "Finland";
    private static final String EN_LANDKODE = "FIN";
    private static final String ET_EIEDOMSNAVN = "Villastrøket";
    private static final String EN_EPOST = "test@epost.com";

    private static final String NORGE = "Norge";

    @InjectMocks
    private PersonaliaFletter personaliaFletter;
    @Mock
    private PersonService personMock;
    @Mock
    private BrukerprofilPortType brukerProfilMock;
    @Mock
    private Kodeverk kodeverkMock;
    @Mock
    private EpostService epostMock;

    private XMLBruker xmlBruker;
    private Person person;
    private DateTimeFormatter dateTimeFormat;

    @Before
    public void setup() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        when(kodeverkMock.getPoststed(EN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getPoststed(EN_ANNEN_ADRESSE_POSTNUMMER)).thenReturn(EN_ADRESSE_POSTSTED);
        when(kodeverkMock.getLand(EN_LANDKODE)).thenReturn(ET_LAND);

        dateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

        HentKjerneinformasjonResponse response = new HentKjerneinformasjonResponse();
        person = genererPersonMedGyldigIdentOgNavn(IDENT, ET_FORNAVN, ET_ETTERNAVN);
        person.setFoedselsdato(fodseldato(1983, 12, 16));
        List<Familierelasjon> familieRelasjoner = person.getHarFraRolleI();
        Familierelasjon familierelasjon = new Familierelasjon();
        Person barn1 = genererPersonMedGyldigIdentOgNavn(BARN_IDENT, BARN_FORNAVN, BARN_ETTERNAVN);

        familierelasjon.setTilPerson(barn1);
        Familierelasjoner familieRelasjonRolle = new Familierelasjoner();

        familieRelasjonRolle.setValue("BARN");
        familierelasjon.setTilRolle(familieRelasjonRolle);

        familieRelasjoner.add(familierelasjon);
        response.setPerson(person);
        when(personMock.hentKjerneinformasjon(any(String.class))).thenReturn(response);

        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlBruker = new XMLBruker().withElektroniskKommunikasjonskanal(lagElektroniskKommunikasjonskanal());
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        preferanserResponse.setPerson(xmlBruker);
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(preferanserResponse);

        WSHentDigitalKontaktinformasjonResponse digitalKontaktinformasjonResponse = new WSHentDigitalKontaktinformasjonResponse();
        digitalKontaktinformasjonResponse.setDigitalKontaktinformasjon(genererDigitalKontaktinformasjonMedEpost());

        when(epostMock.hentInfoFraDKIF(any(String.class))).thenReturn(digitalKontaktinformasjonResponse);
    }

    @Test
    public void returnererPersonaliaObjektDersomPersonenSomReturneresHarRiktigIdent() {
        mockGyldigPerson();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        assertNotNull(personalia);
        assertEquals(IDENT, personalia.getFnr());
        assertEquals(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN, personalia.getNavn());
    }

    @Test
    public void returnererPersonObjektMedStatsborgerskapUtenEpostOgBarn() throws HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = new XMLHentKontaktinformasjonOgPreferanserResponse();
        xmlBruker = new XMLBruker();
        XMLNorskIdent xmlNorskIdent = new XMLNorskIdent();
        xmlNorskIdent.setIdent(IDENT);
        xmlBruker.setIdent(xmlNorskIdent);
        preferanserResponse.setPerson(xmlBruker);
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenReturn(preferanserResponse);

        WSHentDigitalKontaktinformasjonResponse digitalKontaktinformasjonResponse = new WSHentDigitalKontaktinformasjonResponse();
        digitalKontaktinformasjonResponse.setDigitalKontaktinformasjon(genererDigitalKontaktinformasjonUtenEpost());
        when(epostMock.hentInfoFraDKIF(any(String.class))).thenReturn(digitalKontaktinformasjonResponse);

        mockGyldigPerson();

        Statsborgerskap statsborgerskap = new Statsborgerskap();
        Landkoder landkode = new Landkoder();
        landkode.setValue("DNK");
        statsborgerskap.setLand(landkode);
        person.setStatsborgerskap(statsborgerskap);

        XMLPreferanser preferanser = new XMLPreferanser();
        XMLElektroniskKommunikasjonskanal elektroniskKommKanal = new XMLElektroniskKommunikasjonskanal();
        elektroniskKommKanal.setElektroniskAdresse(null);
        preferanser.setForetrukketElektroniskKommunikasjonskanal(elektroniskKommKanal);
        xmlBruker.setPreferanser(preferanser);

        List<Familierelasjon> familierelasjoner = person.getHarFraRolleI();
        familierelasjoner.clear();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        assertNotNull(personalia);
        assertEquals(IDENT, personalia.getFnr());
        assertEquals(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN, personalia.getNavn());

        assertEquals("DNK", personalia.getStatsborgerskap());
        assertEquals("", personalia.getEpost());
    }


    @Test
    public void skalStottePersonerUtenMellomnavn() {
        mockGyldigPersonUtenMellomnavn();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        assertNotNull(personalia);
        assertEquals(ET_FORNAVN + " " + ET_ETTERNAVN, personalia.getNavn());
    }

    @Test
    public void skalStottePersonerUtenNavn() {
        mockGyldigPersonUtenNavn();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        assertNotNull(personalia);
        assertEquals("", personalia.getNavn());
    }

    @Test
    public void returnererPersonObjektMedAdresseInformasjon() {
        String forventetGjeldendeAdresse = EN_ADRESSE_GATE + " " + EN_ADRESSE_HUSNUMMER + EN_ADRESSE_HUSBOKSTAV + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        String forventetSekunarAdresse = "C/O " + EN_POSTBOKS_ADRESSEEIER + ", " + EN_ANNEN_ADRESSE_GATE + " " + EN_ANNEN_ADRESSE_HUSNUMMER + EN_ANNEN_ADRESSE_HUSBOKSTAV + ", " + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        mockGyldigPersonMedAdresse();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);
        assertNotNull(personalia);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        assertNotNull(gjeldendeAdresse);
        assertNotNull(sekundarAdresse);
        assertTrue(personalia.harNorskMidlertidigAdresse());

        assertEquals(Adressetype.BOSTEDSADRESSE.name(), gjeldendeAdresse.getAdressetype());
        assertEquals(forventetGjeldendeAdresse, gjeldendeAdresse.getAdresse());

        assertEquals(Adressetype.MIDLERTIDIG_POSTADRESSE_NORGE.name(), sekundarAdresse.getAdressetype());
        assertEquals(forventetSekunarAdresse, sekundarAdresse.getAdresse());

        assertEquals(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_FRA), sekundarAdresse.getGyldigFra());
        assertEquals(dateTimeFormat.print(EN_ANNEN_ADRESSE_GYLDIG_TIL), sekundarAdresse.getGyldigTil());
    }

    @Test
    public void returnererPersonObjektMedTomAdresseInformasjonVedDiskresjonskoder() {
        mockGyldigPersonMedAdresse();
        xmlBruker.setDiskresjonskode(new XMLDiskresjonskoder().withValue("6"));

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);
        assertNotNull(personalia);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();
        Adresse sekundarAdresse = personalia.getSekundarAdresse();

        assertNull(gjeldendeAdresse.getAdresse());
        assertNull(sekundarAdresse.getAdresse());
    }

    @Test
    public void skalStotteMidlertidigOmrodeAdresseNorge() {
        String forventetsekundarAdresse = ET_EIEDOMSNAVN + ", " + EN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;
        mockGyldigPersonMedMidlertidigOmrodeAdresse();
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse);
        assertEquals(forventetsekundarAdresse, gjeldendeAdresse.getAdresse());
    }

    @Test
    public void skalStotteMidlertidigPostboksAdresseNorge() {
        String forventetgjeldendeAdresse = "C/O " + EN_POSTBOKS_ADRESSEEIER + ", Postboks " + EN_POSTBOKS_NUMMER  + " " + ET_POSTBOKS_NAVN + ", " + EN_ANNEN_ADRESSE_POSTNUMMER + " " + EN_ADRESSE_POSTSTED;

        mockGyldigPersonMedMidlertidigPostboksAdresse();
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse);
        assertEquals(forventetgjeldendeAdresse, gjeldendeAdresse.getAdresse());
    }

    @Test
    public void skalStotteFolkeregistretUtenlandskAdresse() {
        String forventetAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", " + EN_FJERDE_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedUtenlandskFolkeregistrertAdresse();

        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);

        Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(gjeldendeAdresse.getAdresse());

        Assert.assertFalse(personalia.harNorskMidlertidigAdresse());
        Assert.assertTrue(personalia.harUtenlandskFolkeregistrertAdresse());

        assertEquals(forventetAdresse, gjeldendeAdresse.getAdresse());
    }

    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed1Linjer() {
        String forventetAdresse = EN_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(1);
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);
        Adresse sekundarAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertEquals(forventetAdresse, sekundarAdresse.getAdresse());
    }

    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed2Linjer() {
        String forventetAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(2);
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);
        Adresse sekundarAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertEquals(forventetAdresse, sekundarAdresse.getAdresse());
    }

    @Test
    public void skalStotteMidlertidigUtenlandskMidlertidigAdresseMed3Linjer() {
        String forventetAdresse = EN_ADRESSELINJE + ", " + EN_ANNEN_ADRESSELINJE + ", " + EN_TREDJE_ADRESSELINJE + ", " + ET_LAND;

        mockGyldigPersonMedMidlertidigUtenlandskAdresse(3);
        Personalia personalia = personaliaFletter.mapTilPersonalia(IDENT);
        Adresse sekundarAdresse = personalia.getGjeldendeAdresse();

        Assert.assertNotNull(sekundarAdresse.getAdresse());
        assertEquals(forventetAdresse, sekundarAdresse.getAdresse());
    }

    @Test(expected = SendSoknadException.class)
    public void kasterExceptionVedTpsFeil() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenThrow(new WebServiceException());

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    @Test(expected = SendSoknadException.class)
    public void kasterExceptionVedManglendePerson() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenThrow(new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet());

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    @Test(expected = SendSoknadException.class)
    public void kasterExceptionVedSikkerhetsbegrensing() throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning {
        when(brukerProfilMock.hentKontaktinformasjonOgPreferanser(any(XMLHentKontaktinformasjonOgPreferanserRequest.class))).thenThrow(new HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning());

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    @Test(expected = SendSoknadException.class)
    public void kasterExceptionVedWebserviceFeilIPersonTjeneste() {
        when(personMock.hentKjerneinformasjon(any(String.class))).thenThrow(new WebServiceException());

        personaliaFletter.mapTilPersonalia(IDENT);
    }

    private void mockGyldigPersonMedMidlertidigUtenlandskAdresse(int adresselinjer) {
        XMLMidlertidigPostadresseUtland xmlMidlertidigPostadresseUtland = new XMLMidlertidigPostadresseUtland();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(adresselinjer);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_UTLAND");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);

        xmlMidlertidigPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);

        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigPostadresseUtland);
    }

    private void mockGyldigPersonMedUtenlandskFolkeregistrertAdresse() {
        XMLPostadresse xmlPostadresseUtland = new XMLPostadresse();
        XMLUstrukturertAdresse utenlandskUstrukturertAdresse = generateUstrukturertAdresseMedXAntallAdersseLinjer(4);

        XMLLandkoder xmlLandkode = new XMLLandkoder();
        xmlLandkode.setValue(EN_LANDKODE);
        utenlandskUstrukturertAdresse.setLandkode(xmlLandkode);

        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("UTENLANDSK_ADRESSE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlPostadresseUtland.setUstrukturertAdresse(utenlandskUstrukturertAdresse);

        xmlBruker.setPostadresse(xmlPostadresseUtland);
    }

    private void mockGyldigPersonMedMidlertidigOmrodeAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigOmrodeAdresseNorge = generateMidlertidigOmrodeAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigOmrodeAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void mockGyldigPersonMedMidlertidigPostboksAdresse() {
        XMLMidlertidigPostadresseNorge midlertidigPostboksAdresseNorge = generateMidlertidigPostboksAdresseNorge();
        xmlBruker.setMidlertidigPostadresse(midlertidigPostboksAdresseNorge);
        XMLPostadressetyper xmlPostadresseType = new XMLPostadressetyper();
        xmlPostadresseType.setValue("MIDLERTIDIG_POSTADRESSE_NORGE");
        xmlBruker.setGjeldendePostadresseType(xmlPostadresseType);
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }

    private void mockGyldigPersonMedAdresse() {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
        XMLBostedsadresse bostedsadresse = genererXMLFolkeregistrertAdresse(true);
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = generateMidlertidigAdresseNorge();

        XMLPostadressetyper postadressetyper = new XMLPostadressetyper();
        postadressetyper.setValue(FOLKEREGISTRERT_ADRESSE_VALUE);

        xmlBruker.setBostedsadresse(bostedsadresse);
        xmlBruker.setGjeldendePostadresseType(postadressetyper);
        xmlBruker.setMidlertidigPostadresse(xmlMidlertidigNorge);
    }

    private void mockGyldigPersonUtenNavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        xmlBruker.setPersonnavn(personNavn);
    }

    private void mockGyldigPersonUtenMellomnavn() {
        xmlBruker.setPersonnavn(navnUtenMellomnavn());
    }

    private void mockGyldigPerson() {
        xmlBruker.setPersonnavn(navnMedMellomnavn());
    }


    private XMLMidlertidigPostadresseNorge generateMidlertidigAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigNorge.setPostleveringsPeriode(xmlGyldighetsperiode);
        XMLGateadresse xmlgateadresse = new XMLGateadresse();
        xmlgateadresse.setTilleggsadresse(EN_POSTBOKS_ADRESSEEIER);
        xmlgateadresse.setGatenavn(EN_ANNEN_ADRESSE_GATE);
        xmlgateadresse.setHusnummer(new BigInteger(EN_ANNEN_ADRESSE_HUSNUMMER));
        xmlgateadresse.setHusbokstav(EN_ANNEN_ADRESSE_HUSBOKSTAV);
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
        xmlgateadresse.setPoststed(xmlpostnummer);
        xmlMidlertidigNorge.setStrukturertAdresse(xmlgateadresse);
        return xmlMidlertidigNorge;
    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigOmrodeAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigPostadresse = new XMLMidlertidigPostadresseNorge();

        XMLMatrikkeladresse xmlMatrikkelAdresse = new XMLMatrikkeladresse();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigPostadresse.setPostleveringsPeriode(xmlGyldighetsperiode);

        xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
        xmlMatrikkelAdresse.setPoststed(xmlpostnummer);
        xmlMatrikkelAdresse.setEiendomsnavn(ET_EIEDOMSNAVN);

        xmlMidlertidigPostadresse.setStrukturertAdresse(xmlMatrikkelAdresse);
        return xmlMidlertidigPostadresse;
    }

    private XMLMidlertidigPostadresseNorge generateMidlertidigPostboksAdresseNorge() {
        XMLMidlertidigPostadresseNorge xmlMidlertidigNorge = new XMLMidlertidigPostadresseNorge();
        XMLGyldighetsperiode xmlGyldighetsperiode = generateGyldighetsperiode(true);
        xmlMidlertidigNorge.setPostleveringsPeriode(xmlGyldighetsperiode);

        XMLPostboksadresseNorsk xmlPostboksAdresse = new XMLPostboksadresseNorsk();
        xmlPostboksAdresse.setPostboksanlegg(ET_POSTBOKS_NAVN);
        xmlPostboksAdresse.setPostboksnummer(EN_POSTBOKS_NUMMER);
        xmlPostboksAdresse.setTilleggsadresse(EN_POSTBOKS_ADRESSEEIER);
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        xmlpostnummer.setValue(EN_ANNEN_ADRESSE_POSTNUMMER);
        xmlPostboksAdresse.setPoststed(xmlpostnummer);
        xmlMidlertidigNorge.setStrukturertAdresse(xmlPostboksAdresse);
        return xmlMidlertidigNorge;
    }

    private XMLGyldighetsperiode generateGyldighetsperiode(boolean harFraDato) {
        XMLGyldighetsperiode xmlGyldighetsperiode = new XMLGyldighetsperiode();
        if (harFraDato) {
            xmlGyldighetsperiode.setFom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_FRA));
        }
        xmlGyldighetsperiode.setTom(new DateTime(EN_ANNEN_ADRESSE_GYLDIG_TIL));
        return xmlGyldighetsperiode;
    }

    private XMLBostedsadresse genererXMLFolkeregistrertAdresse(boolean medData) {
        XMLBostedsadresse bostedsadresse = new XMLBostedsadresse();
        XMLGateadresse gateadresse = new XMLGateadresse();
        XMLPostnummer xmlpostnummer = new XMLPostnummer();
        if (medData) {
            gateadresse.setGatenavn(EN_ADRESSE_GATE);
            gateadresse.setHusnummer(new BigInteger(EN_ADRESSE_HUSNUMMER));
            gateadresse.setHusbokstav(EN_ADRESSE_HUSBOKSTAV);
            xmlpostnummer.setValue(EN_ADRESSE_POSTNUMMER);
        }
        gateadresse.setPoststed(xmlpostnummer);
        gateadresse.setLandkode(lagLandkode());
        bostedsadresse.setStrukturertAdresse(gateadresse);
        return bostedsadresse;
    }

    private XMLUstrukturertAdresse generateUstrukturertAdresseMedXAntallAdersseLinjer(int antallAdresseLinjer) {
        XMLUstrukturertAdresse ustrukturertAdresse = new XMLUstrukturertAdresse();
        switch (antallAdresseLinjer) {
            case 0:
                break;
            case 1:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                break;
            case 2:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                break;
            case 3:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje3(EN_TREDJE_ADRESSELINJE);
                break;
            case 4:
                ustrukturertAdresse.setAdresselinje1(EN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje2(EN_ANNEN_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje3(EN_TREDJE_ADRESSELINJE);
                ustrukturertAdresse.setAdresselinje4(EN_FJERDE_ADRESSELINJE);
                break;
            default:
                break;
        }

        return ustrukturertAdresse;
    }

    private static XMLElektroniskKommunikasjonskanal lagElektroniskKommunikasjonskanal() {
        return new XMLElektroniskKommunikasjonskanal().withElektroniskAdresse(lagElektroniskAdresse());
    }

    private static XMLElektroniskAdresse lagElektroniskAdresse() {
        return new XMLEPost().withIdentifikator(EN_EPOST);
    }

    private static XMLLandkoder lagLandkode() {
        return new XMLLandkoder().withValue(NORGE);
    }

    private static XMLPersonnavn navnMedMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setMellomnavn(ET_MELLOMNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_MELLOMNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
    }

    private static XMLPersonnavn navnUtenMellomnavn() {
        XMLPersonnavn personNavn = new XMLPersonnavn();
        personNavn.setFornavn(ET_FORNAVN);
        personNavn.setSammensattNavn(ET_FORNAVN + " " + ET_ETTERNAVN);
        personNavn.setEtternavn(ET_ETTERNAVN);
        return personNavn;
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

    private static WSKontaktinformasjon genererDigitalKontaktinformasjonMedEpost() {
        return new WSKontaktinformasjon()
                .withPersonident(IDENT)
                .withEpostadresse(new WSEpostadresse().withValue("test@test.com"))
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue("12345678"))
                .withReservasjon("");
    }

    private static WSKontaktinformasjon genererDigitalKontaktinformasjonUtenEpost() {
        return new WSKontaktinformasjon()
                .withPersonident(IDENT)
                .withMobiltelefonnummer(new WSMobiltelefonnummer().withValue("12345678"))
                .withReservasjon("");
    }
}
