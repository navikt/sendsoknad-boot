package no.nav.sbl.dialogarena.sendsoknad.domain.message;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;

public class TekstHenterTest {

    private static final Locale LOCALE_NB = LocaleUtils.toLocale("nb_NO");
    private static final Locale LOCALE_NN = LocaleUtils.toLocale("nn_NO");
    private static final Locale LOCALE_EN = LocaleUtils.toLocale("en");

    private static final String NOKKEL_AAP = "aap.gjenopptak.skjema.tittel";
    private static final String NOKKEL_FELLES = "kvittering.innsendt.tittel";
    private static final String NOKKEL_BILSTONAD = "bilen.skalAnskaffeNyBil.sporsmal";
    private static final String NOKKEL_TILTAKSPENGER = "informasjonsside.tiltakspenger.informasjon.overskrift";

    private TekstHenter tekstHenter;

    @Before
    public void setUp() {
        tekstHenter = new TekstHenter()
                .lesInnTeksterFraDiskForLocale(LOCALE_NB)
                .lesInnTeksterFraDiskForLocale(LOCALE_NN)
                .lesInnTeksterFraDiskForLocale(LOCALE_EN);
    }

    @Test
    public void skalLeseInnAlleTyperTeksterforNb() {
        assertEquals(7, tekstHenter.getTeksterForNb().size());
    }

    @Test
    public void skalLeseInnAlleTyperTeksterForNn() {
        assertEquals(2, tekstHenter.getTeksterForNn().size());
        assertTrue(tekstHenter.getTeksterForNn().containsKey("soknadaap"));
    }

    @Test
    public void skalLeseInnAlleTyperTeksterForEn() {
        assertEquals(1, tekstHenter.getTeksterForEn().size());
        assertTrue(tekstHenter.getTeksterForEn().containsKey("sendsoknad"));
    }

    @Test
    public void skalHenteBundleForIllegalNokkel() {
        Properties tekstBundle = tekstHenter.getBundleFor("illegal key", LOCALE_NB);
        assertTrue(tekstBundle.containsKey(NOKKEL_FELLES));
    }

    @Test
    public void skalHenteBundleForTiltakspengerMedFellesteksterNb() {
        Properties tekstBundle = tekstHenter.getBundleFor("tiltakspenger", LOCALE_NB);
        assertTrue(tekstBundle.containsKey(NOKKEL_TILTAKSPENGER));
        assertTrue(tekstBundle.containsKey(NOKKEL_FELLES));
        assertFalse(tekstBundle.containsKey(NOKKEL_BILSTONAD));
    }

    @Test
    public void skalHenteBundleForBilstonadMedFellesteksterNb() {
        Properties tekstBundle = tekstHenter.getBundleFor("bilstonad", LOCALE_NB);
        assertTrue(tekstBundle.containsKey(NOKKEL_BILSTONAD));
        assertTrue(tekstBundle.containsKey(NOKKEL_FELLES));
        assertFalse(tekstBundle.containsKey(NOKKEL_TILTAKSPENGER));
    }

    @Test
    public void skalHenteBundleForAapMedFellesteksterNn() {
        Properties tekstBundle = tekstHenter.getBundleFor("soknadaap", LOCALE_NN);
        assertTrue(tekstBundle.containsKey(NOKKEL_AAP));
        assertTrue(tekstBundle.containsKey(NOKKEL_FELLES));
        assertFalse(tekstBundle.containsKey(NOKKEL_TILTAKSPENGER));
    }

    @Test
    public void skalMergeAlleTeksterVedKallUtenType() {
        Properties tekstBundle = tekstHenter.getBundleFor("", LOCALE_NB);
        assertTrue(tekstBundle.containsKey(NOKKEL_AAP));
        assertTrue(tekstBundle.containsKey(NOKKEL_BILSTONAD));
        assertTrue(tekstBundle.containsKey(NOKKEL_TILTAKSPENGER));
        assertTrue(tekstBundle.containsKey(NOKKEL_FELLES));
    }

    @Test
    public void skalHenteTekstRiktigMedNullArgs() {
        String tekst = tekstHenter.finnTekst("fortsettSenere.epost.label", new Object[]{}, LOCALE_NB);
        assertEquals("Send meg en lenke på e-post (ikke påkrevd)", tekst);
    }

    @Test
    public void skalHenteTekstRiktigMedArgs() {
        String tekst = tekstHenter.finnTekst("kvittering.erSendt",
                new Object[]{"Test1", "Test2", "Test3", "Test4"}, LOCALE_NB);

        assertEquals("Test1 av Test2 vedlegg ble sendt til NAV Test3, klokken Test4", tekst);
    }
}
