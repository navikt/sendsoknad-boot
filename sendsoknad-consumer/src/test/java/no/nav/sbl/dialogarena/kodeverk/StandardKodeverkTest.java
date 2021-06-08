package no.nav.sbl.dialogarena.kodeverk;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLEnkeltKodeverk;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLKode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLPeriode;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.XMLTerm;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkResponse;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardKodeverkTest {

    @Mock
    private KodeverkPortType ws;

    private Kodeverk kodeverk;

    private final File dumpDir = Paths.get("target/kodeverkdump/" + randomNumeric(10)).toAbsolutePath().toFile();

    @Before
    public void wireUpKodeverk() {
        if (!dumpDir.exists())
            assertTrue(dumpDir.mkdirs());
        kodeverk = new StandardKodeverk(ws, Locale.getDefault(), Optional.of(dumpDir));
    }

    @Test
    public void kanHentePoststedBasertPaaPostnummer() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        XMLHentKodeverkResponse response = postnummerKodeverkResponse();
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(response);

        String poststed = kodeverk.getPoststed("0565");
        assertEquals("Oslo", poststed);
    }

    @Test
    public void skalKunneSlaaOppTermBasertPaaKodeOgOmvendt() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landKodeverkMedUlikeGyldighetsperioder());
        assertEquals("Norge", kodeverk.getLand("NOR"));
    }

    @Test
    public void skalFiltrereVekkUgyldigePerioder() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landKodeverkMedUlikeGyldighetsperioder());
        assertEquals("Norge", kodeverk.getLand("NOR"));
        assertNull(kodeverk.getLand("SUN"));
        assertEquals("Myanmar (burma)", kodeverk.getLand("MMR"));
        assertNull(kodeverk.getLand("YUG"));
    }

    @Test
    public void skalReturnereLandkoderSortertEtterTerm() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());
        List<String> strings = kodeverk.hentAlleKodenavnFraKodeverk(Kodeverk.EksponertKodeverk.LANDKODE);
        assertTrue(strings.containsAll(asList("ALB", "DNK", "NOR", "SWE", "OST","ALA")));
    }

    @Test(expected = SendSoknadException.class)
    public void ugyldigKodeverknavnGirSystemException() throws HentKodeverkHentKodeverkKodeverkIkkeFunnet {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenThrow(new HentKodeverkHentKodeverkKodeverkIkkeFunnet());
        kodeverk.lastInnNyeKodeverk();
    }

    @Test
    public void dumperInnlastetKodeverkTilFileOgBrukerDenneVedRestartDaKodeverkErNede() throws Exception {
        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenReturn(landkodeKodeverkResponse());
        kodeverk.lastInnNyeKodeverk();

        when(ws.hentKodeverk(any(XMLHentKodeverkRequest.class))).thenThrow(new RuntimeException("Kodeverk er nede"));
        kodeverk.lastInnNyeKodeverk();
    }


    private XMLHentKodeverkResponse postnummerKodeverkResponse() {
        XMLKode kode = new XMLKode()
                .withNavn("0565")
                .withTerm(new XMLTerm().withNavn("Oslo").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Kommuner").withKode(kode));
    }

    private static XMLHentKodeverkResponse landkodeKodeverkResponse() {
        XMLKode norge = new XMLKode().withNavn("NOR").withTerm(new XMLTerm().withNavn("Norge").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode aaland = new XMLKode().withNavn("ALA").withTerm(new XMLTerm().withNavn("Åland").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode sverige = new XMLKode().withNavn("SWE").withTerm(new XMLTerm().withNavn("Sverige").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode oesttemor = new XMLKode().withNavn("OST").withTerm(new XMLTerm().withNavn("Øst-Temor").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode albania = new XMLKode().withNavn("ALB").withTerm(new XMLTerm().withNavn("Albania").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode danmark = new XMLKode().withNavn("DNK").withTerm(new XMLTerm().withNavn("Danmark").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(1)).withTom(DateMidnight.now().plusDays(1)));
        XMLKode sovjetunionen = new XMLKode().withNavn("SUN").withTerm(new XMLTerm().withNavn("Sovjetunionen").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(2)).withTom(DateMidnight.now().minusDays(1))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().minusDays(2)).withTom(DateMidnight.now().plusDays(1)));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, aaland, oesttemor, sverige, albania, danmark, sovjetunionen));
    }


    /**
     * Kodeverket for land utdaterer ikke kode sin gyldighetsperiode, kun koden sin term sin gyldighetsperiode,
     * f.eks. kode SUN har gyldighets fram til år 9999, men en termen Sovjetunionen har gyldighet fram til 2000
     * @return XMLHentKodeverkResponse
     */
    private static XMLHentKodeverkResponse landKodeverkMedUlikeGyldighetsperioder() {
        XMLKode norge = new XMLKode().withNavn("NOR")
                .withTerm(new XMLTerm().withNavn("Norge").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1905-06-07")).withTom(DateMidnight.parse("9999-12-31"))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1905-06-07")).withTom(DateMidnight.parse("9999-12-31")));
        XMLKode myanmar = new XMLKode().withNavn("MMR")
                .withTerm(new XMLTerm().withNavn("Burma").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1900-01-01")).withTom(DateMidnight.parse("1988-12-31"))))
                .withTerm(new XMLTerm().withNavn("Myanmar (Burma)").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1989-01-01")).withTom(DateMidnight.parse("9999-12-31"))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1901-01-01")).withTom(DateMidnight.parse("9999-12-31")));
        XMLKode sovjetunionen = new XMLKode().withNavn("SUN")
                .withTerm(new XMLTerm().withNavn("Sovjetunionen").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1922-12-30")).withTom(DateMidnight.parse("2000-01-01"))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1901-01-01")).withTom(DateMidnight.parse("9999-12-31")));
        XMLKode jugoslavia = new XMLKode().withNavn("YUG")
                .withTerm(new XMLTerm().withNavn("Jugoslavia").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1945-05-08")).withTom(DateMidnight.parse("1992-01-01"))))
                .withTerm(new XMLTerm().withNavn("Nye-Jugoslavia").withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.now().plusDays(1)).withTom(DateMidnight.parse("9999-12-31"))))
                .withGyldighetsperiode(new XMLPeriode().withFom(DateMidnight.parse("1901-01-01")).withTom(DateMidnight.parse("9999-12-31")));

        return new XMLHentKodeverkResponse().withKodeverk(new XMLEnkeltKodeverk().withNavn("Landkoder").withKode(norge, myanmar, sovjetunionen, jugoslavia));
    }
}
