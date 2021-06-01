package no.nav.sbl.dialogarena.sendsoknad.domain;

import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class WebSoknadTest {

    private static final String SKJEMANUMMER_KVITTERING = "L7";

    private WebSoknad soknad;
    private Long soknadId;
    private Long faktumId;

    @Before
    public void setUp() {
        soknadId = 2L;
        faktumId = 33L;
        soknad = new WebSoknad();
        soknad.setSoknadId(soknadId);
    }

    @Test
    public void shouldKunneLageTomSoknad() {
        assertEquals(0, soknad.antallFakta());
    }

    @Test
    public void skalKunneLeggeTilFakta() {
        soknad.leggTilFaktum(new Faktum().medSoknadId(soknadId).medFaktumId(faktumId).medKey("enKey").medValue("enValue"));
        assertEquals(1, soknad.antallFakta());
    }

    @Test
    public void skalReturnereTrueDersomSoknadHarN6VedleggSomIkkeErLastetOpp() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer(SKJEMANUMMER_KVITTERING)
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        soknad.setVedlegg(vedlegg);
        assertTrue(soknad.harAnnetVedleggSomIkkeErLastetOpp());
    }

    @Test
    public void skalReturnereFalseDersomSoknadIkkeHarN6VedleggSomIkkeErLastetOpp() {
        List<Vedlegg> vedlegg = Arrays.asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(0L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("uidVedlegg1")
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medStorrelse(5L)
                        .medNavn("Test Annet vedlegg")
                        .medAntallSider(3),
                new Vedlegg()
                        .medSkjemaNummer(SKJEMANUMMER_KVITTERING)
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke));

        soknad.setVedlegg(vedlegg);
        assertFalse(soknad.harAnnetVedleggSomIkkeErLastetOpp());
    }

    @Test
    public void skalUnmarshalleMellomlagredeSoknaderMedFaktaElementer() {
        InputStream soknadMedFaktaElementer = WebSoknadTest.class.getResourceAsStream("/soknader/soknad-struktur-fakta.xml");
        WebSoknad soknad = JAXB.unmarshal(soknadMedFaktaElementer, WebSoknad.class);
        assertEquals(3L, soknad.antallFakta());
    }

    @Test
    public void skalUnmarshalleGamleMellomlagredeSoknaderMedFaktalisteElementer() {
        InputStream soknadMedFaktalisteElementer = WebSoknad.class.getResourceAsStream("/soknader/soknad-struktur-faktaListe.xml");
        WebSoknad soknad = JAXB.unmarshal(soknadMedFaktalisteElementer, WebSoknad.class);
        assertEquals(3L, soknad.antallFakta());
    }

    @Test
    public void skalMarshalleSoknaderTilFaktaElementer() {
        InputStream gammelStruktur = WebSoknad.class.getResourceAsStream("/soknader/soknad-struktur-faktaListe.xml");
        WebSoknad soknad = JAXB.unmarshal(gammelStruktur, WebSoknad.class);

        OutputStream output = new ByteArrayOutputStream();
        JAXB.marshal(soknad, output);
        String xml = output.toString();
        assertTrue(xml.contains("<fakta>"));
        assertFalse(xml.contains("<faktaListe>"));
    }

    @Test
    public void skalReturnereVedleggSomMatcherForventningMedFlereTillattOgLikFaktumid() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId));
    }

    @Test
    public void skalReturnereVedleggSomMatcherForventningMedIkkeFlereTillattOgIkkeFaktumid() {
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(false);

        Vedlegg vedlegg = new Vedlegg(1L, null, skjemanummer, Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, null));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeHarFaktumIdOgFlereTillatt() {
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);
        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);

        assertNotEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, null));
    }

    @Test
    public void skalIkkeReturnereVedleggSomHarUlikeFaktumIdFraForventning() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.UnderBehandling);
        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        long ulikFaktumid = 1L;
        assertNotEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, ulikFaktumid));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeMatcherForventningPaSkjemanummer() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, "K4", Vedlegg.Status.UnderBehandling);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertNotEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeMatcherForventningPaSkjemanummertillegg() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);

        forventning.setFlereTillatt(true);
        forventning.setSkjemanummerTillegg("skjemanummertillegg");

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, "K4", Vedlegg.Status.UnderBehandling);
        vedlegg.medSkjemanummerTillegg("annetSkjemanummertillegg");

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertNotEquals(vedlegg, webSoknad.finnVedleggSomMatcherForventning(forventning, faktumId));
    }

    @Test
    public void skalIkkeReturnereVedleggSomIkkeTillOppsummering() {
        Long faktumId = 123456L;
        String skjemanummer = "M6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);

        forventning.setFlereTillatt(true);
        forventning.setSkjemanummerTillegg("skjemanummertillegg");

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, "K4", Vedlegg.Status.VedleggKreves);
        vedlegg.medSkjemanummerTillegg("annetSkjemanummertillegg");

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertNotEquals(vedlegg, webSoknad.finnVedleggMatcherDelsteg(forventning, faktumId));
    }

    @Test
    public void skalReturnereVedleggSomSkalTilOppsummering() {
        Long faktumId = 123456L;
        String skjemanummer = "N6";
        VedleggForFaktumStruktur forventning = new VedleggForFaktumStruktur()
                .medSkjemanummer(skjemanummer);
        forventning.setFlereTillatt(true);

        Vedlegg vedlegg = new Vedlegg(1L, faktumId, skjemanummer, Vedlegg.Status.SendesSenere);

        WebSoknad webSoknad = new WebSoknad().medVedlegg(vedlegg);
        assertEquals(vedlegg, webSoknad.finnVedleggMatcherDelsteg(forventning, faktumId));
    }
}
