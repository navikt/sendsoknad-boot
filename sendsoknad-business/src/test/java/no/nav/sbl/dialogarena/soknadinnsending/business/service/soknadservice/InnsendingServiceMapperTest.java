package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendingServiceMapper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;

public class InnsendingServiceMapperTest {

    private static final String TEMA = "TSO";
    private static final String TITTEL = "Kjøreliste for godkjent bruk av egen bil";
    private static final String N6_TITTEL = "Annet";
    private static final String L8_TITTEL = "Uttalelse fra fagpersonell";
    private static final String BEHANDLINGSID = "123";
    private static final String AKTORID = "123456";
    private static final String ID_HOVEDSKJEMA = "idHovedskjema";
    private static final String SKJEMANUMMER = "NAV 11-12.10";

    private static final byte[] CONTENT_UNKNOWN = {4, 5, 6};
    private static final byte[] CONTENT_PDFA = getBytesFromFile("/pdfs/pdfa.pdf");
    private static final byte[] CONTENT_PDF = getBytesFromFile("/pdfs/navskjema.pdf");

    @BeforeAll
    public static void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
    }


    @Test
    public void testHovedSkjemadata_PdfAndNull() {
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        List<Hovedskjemadata> hovedskjemadata = createHovedskjemas(webSoknad, CONTENT_PDF, null, fullSoknadId, emptyList());

        assertEquals(1, hovedskjemadata.size());
        assertHovedskjemadata(new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"), hovedskjemadata.get(0));
    }

    @Test
    public void testHovedSkjemadata_PdfAndUnknown() {
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        List<Hovedskjemadata> hovedskjemadata = createHovedskjemas(webSoknad, CONTENT_PDF, CONTENT_UNKNOWN, fullSoknadId, emptyList());

        assertEquals(2, hovedskjemadata.size());
        assertHovedskjemadata(new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"), hovedskjemadata.get(0));
        assertHovedskjemadata(new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"), hovedskjemadata.get(1));
    }

    @Test
    public void testHovedSkjemadata_PdfaAndPdf() {
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        List<Hovedskjemadata> hovedskjemadata = createHovedskjemas(webSoknad, CONTENT_PDFA, CONTENT_PDF, fullSoknadId, emptyList());

        assertEquals(2, hovedskjemadata.size());
        assertHovedskjemadata(new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF/A", SKJEMANUMMER + ".pdfa"), hovedskjemadata.get(0));
        assertHovedskjemadata(new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"), hovedskjemadata.get(1));
    }

    @Test
    public void testHovedSkjemadata_UnknownAndPdfa() {
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        List<Hovedskjemadata> hovedskjemadata = createHovedskjemas(webSoknad, CONTENT_UNKNOWN, CONTENT_PDFA, fullSoknadId, emptyList());

        assertEquals(2, hovedskjemadata.size());
        assertHovedskjemadata(new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", DEFAULT_FILE_TYPE, SKJEMANUMMER + "." + DEFAULT_FILE_TYPE.toLowerCase()), hovedskjemadata.get(0));
        assertHovedskjemadata(new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"), hovedskjemadata.get(1));
    }

    @Test
    public void testAlternativeRepresentations() {
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        List<AlternativRepresentasjon> alternativeRepresentations = asList(
                new AlternativRepresentasjon()
                        .medRepresentasjonsType(AlternativRepresentasjonType.JSON)
                        .medMimetype(APPLICATION_JSON_VALUE)
                        .medFilnavn("tiltakspenger.json")
                        .medUuid("altRepId0"),
                new AlternativRepresentasjon()
                        .medRepresentasjonsType(AlternativRepresentasjonType.XML)
                        .medMimetype(APPLICATION_XML_VALUE)
                        .medFilnavn("Tilleggsstonader.xml")
                        .medUuid("altRepId1"),
                new AlternativRepresentasjon()
                        .medMimetype("application/pdfa")
                        .medFilnavn("apa.pdfa")
                        .medUuid("altRepId2"),
                new AlternativRepresentasjon()
                        .medMimetype("application/pdf")
                        .medFilnavn("bepa.pdf")
                        .medUuid("altRepId3"),
                new AlternativRepresentasjon()
                        .medMimetype("made up mimetype for testing")
                        .medFilnavn("cepa.bmp")
                        .medUuid("altRepId4"),
                new AlternativRepresentasjon()
                        .medMimetype(APPLICATION_JSON_VALUE)
                        .medFilnavn(null)
                        .medUuid("altRepId5"),
                new AlternativRepresentasjon()
                        .medMimetype(APPLICATION_XML_VALUE)
                        .medFilnavn("")
                        .medUuid("altRepId6"));


        List<Hovedskjemadata> hovedskjemadata = createHovedskjemas(webSoknad, CONTENT_PDF, CONTENT_UNKNOWN, fullSoknadId, alternativeRepresentations);

        assertEquals(2 + alternativeRepresentations.size(), hovedskjemadata.size());
        assertHovedskjemadata(new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"), hovedskjemadata.get(0));
        assertHovedskjemadata(new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"), hovedskjemadata.get(1));
        assertHovedskjemadata(new Hovedskjemadata("altRepId0", APPLICATION_JSON_VALUE, "JSON", "tiltakspenger.json"), hovedskjemadata.get(2));
        assertHovedskjemadata(new Hovedskjemadata("altRepId1", APPLICATION_XML_VALUE, "XML", "Tilleggsstonader.xml"), hovedskjemadata.get(3));
        assertHovedskjemadata(new Hovedskjemadata("altRepId2", "application/pdfa", "PDF", "apa.pdfa"), hovedskjemadata.get(4));
        assertHovedskjemadata(new Hovedskjemadata("altRepId3", "application/pdf", "PDF", "bepa.pdf"), hovedskjemadata.get(5));
        assertHovedskjemadata(new Hovedskjemadata("altRepId4", "made up mimetype for testing", DEFAULT_FILE_TYPE, "cepa.bmp"), hovedskjemadata.get(6));
        assertHovedskjemadata(new Hovedskjemadata("altRepId5", APPLICATION_JSON_VALUE, "JSON", SKJEMANUMMER + ".json"), hovedskjemadata.get(7));
        assertHovedskjemadata(new Hovedskjemadata("altRepId6", APPLICATION_XML_VALUE, "XML", SKJEMANUMMER + ".xml"), hovedskjemadata.get(8));
    }

    @Test
    public void testVedlegg() {
        String vedleggNavn = "vedleggNavn";
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name")
                        .medNavn(vedleggNavn)
                        .medMimetype("")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name = null, skjemanummerTillegg = null")
                        .medNavn(null)
                        .medSkjemanummerTillegg(null)
                        .medFilnavn("jollyjson.json")
                        .medMimetype("application/json")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with blank name, blank skjemanummerTillegg")
                        .medNavn("")
                        .medSkjemanummerTillegg("")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("L8")
                        .medFillagerReferanse("L8")
                        .medSkjemanummerTillegg("Apa Bepa")
                        .medStorrelse(71L));


        List<Vedleggsdata> vedleggsdata = createVedleggdata("", vedlegg);

        assertEquals(vedlegg.size(), vedleggsdata.size());
        assertVedleggsdata(new Vedleggsdata("N6 with name", DEFAULT_VEDLEGG_MIMETYPE, "PDF", vedleggNavn, "N6", vedleggNavn), vedleggsdata.get(0));
        assertVedleggsdata(new Vedleggsdata("N6 with name = null, skjemanummerTillegg = null", "application/json", "JSON", "jollyjson.json", "N6", N6_TITTEL), vedleggsdata.get(1));
        assertVedleggsdata(new Vedleggsdata("N6 with blank name, blank skjemanummerTillegg", DEFAULT_VEDLEGG_MIMETYPE, "PDF", DEFAULT_VEDLEGG_NAME, "N6", N6_TITTEL), vedleggsdata.get(2));
        assertVedleggsdata(new Vedleggsdata("L8", DEFAULT_VEDLEGG_MIMETYPE, "PDF", "L8", "L8", L8_TITTEL + ": Apa Bepa"), vedleggsdata.get(3));
    }

    @Test
    public void testWhenSkjemaOppslagServiceThrowsException_EmptyTitleIsSet() {
        List<Vedlegg> vedlegg = Collections.singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("Non-existing-skjemanummer")
                        .medFillagerReferanse("fillagerReferanse")
                        .medStorrelse(71L));

        List<Vedleggsdata> vedleggsdata = createVedleggdata("", vedlegg);

        assertEquals(vedlegg.size(), vedleggsdata.size());
        assertEquals("", vedleggsdata.get(0).getTittel());
    }

    @Test
    public void testVedleggFileNames() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with Name but null filename")
                        .medFilnavn(null)
                        .medNavn("Apa")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with Name but empty filename")
                        .medFilnavn("")
                        .medNavn("Bepa")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("Cepa")
                        .medFillagerReferanse("Cepa with Name but empty filename")
                        .medFilnavn("")
                        .medNavn("vedleggNavn")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with filename")
                        .medFilnavn("Depa")
                        .medNavn("vedleggNavn")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with empty filename and null name")
                        .medFilnavn("")
                        .medNavn(null)
                        .medStorrelse(71L));


        List<Vedleggsdata> vedleggsdata = createVedleggdata("", vedlegg);

        assertEquals(vedlegg.size(), vedleggsdata.size());
        assertEquals("Apa", vedleggsdata.get(0).getFilename());
        assertEquals("Bepa", vedleggsdata.get(1).getFilename());
        assertEquals("Cepa", vedleggsdata.get(2).getFilename());
        assertEquals("Depa", vedleggsdata.get(3).getFilename());
        assertEquals(DEFAULT_VEDLEGG_NAME, vedleggsdata.get(4).getFilename());
    }

    @Test
    public void testOnlyOpplastedeVedleggAreKept() {
        List<Vedlegg> vedlegg = Arrays.stream(Vedlegg.Status.values())
                .map(status -> new Vedlegg()
                        .medInnsendingsvalg(status)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("Vedlegg er " + status)
                        .medNavn("name_" + status)
                        .medStorrelse(71L))
                .collect(Collectors.toList());


        List<Vedleggsdata> vedleggsdata = createVedleggdata("", vedlegg);

        assertEquals(1, vedleggsdata.size());
        assertEquals("Vedlegg er LastetOpp", vedleggsdata.get(0).getId());
    }

    @Test
    public void testOnlyVedleggWithSizeAreKept() {
        List<Vedlegg> vedlegg = List.of(
                new Vedlegg()
                        .medStorrelse(null)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId0")
                        .medNavn("vedleggWithoutSize"),
                new Vedlegg()
                        .medStorrelse(0L)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId1")
                        .medNavn("vedleggWithSize0"),
                new Vedlegg()
                        .medStorrelse(71L)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId2")
                        .medNavn("vedleggWithSize71"));



        List<Vedleggsdata> vedleggsdata = createVedleggdata("", vedlegg);

        assertEquals(1, vedleggsdata.size());
        assertEquals("vedleggWithSize71", vedleggsdata.get(0).getTittel());
    }

    @Test
    public void testSoknadsdataHasExpectedAttributes() {
        WebSoknad webSoknad = createWebSoknad(emptyList());

        Soknadsdata soknadsdata = createSoknadsdata(webSoknad);

        assertEquals(BEHANDLINGSID, soknadsdata.getBehandlingId());
        assertEquals(SKJEMANUMMER, soknadsdata.getSkjemanummer());
        assertTrue(soknadsdata.getErEttersending());
        assertEquals(AKTORID, soknadsdata.getAktoerId());
        assertEquals(TEMA, soknadsdata.getTema());
        assertEquals(TITTEL, soknadsdata.getTittel());
    }


    private void assertHovedskjemadata(Hovedskjemadata expected, Hovedskjemadata actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMediatype(), actual.getMediatype());
        assertEquals(expected.getFileName(), actual.getFileName());
        assertEquals(expected.getFileType(), actual.getFileType());
    }

    private void assertVedleggsdata(Vedleggsdata expected, Vedleggsdata actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getMediatype(), actual.getMediatype());
        assertEquals(expected.getFilename(), actual.getFilename());
        assertEquals(expected.getFileType(), actual.getFileType());
        assertEquals(expected.getTittel(), actual.getTittel());
        assertEquals(expected.getSkjemanummer(), actual.getSkjemanummer());
    }

    private WebSoknad createWebSoknad(List<Vedlegg> vedlegg) {
        return new WebSoknad()
                .medId(1L)
                .medAktorId(AKTORID)
                .medBehandlingId(BEHANDLINGSID)
                .medBehandlingskjedeId("68")
                .medUuid(ID_HOVEDSKJEMA)
                .medskjemaNummer(SKJEMANUMMER)
                .medFaktum(new Faktum().medKey("personalia"))
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medJournalforendeEnhet("enhet")
                .medVedlegg(vedlegg);
    }

    private static byte[] getBytesFromFile(String path) {
        try (InputStream resourceAsStream = InnsendingServiceMapperTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
