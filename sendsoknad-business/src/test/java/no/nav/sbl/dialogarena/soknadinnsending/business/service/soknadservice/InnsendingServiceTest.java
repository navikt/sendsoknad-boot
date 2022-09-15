package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendingService.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;

public class InnsendingServiceTest {

    private static final String TEMA = "TEMA";
    private static final String TITTEL = "TITTEL";
    private static final String BEHANDLINGSID = "123";
    private static final String AKTORID = "123456";
    private static final String ID_HOVEDSKJEMA = "idHovedskjema";
    private static final String SKJEMANUMMER = "NAV 11-13.05";

    private static final byte[] CONTENT_UNKNOWN = {4, 5, 6};
    private static final byte[] CONTENT_PDFA = getBytesFromFile("/pdfs/pdfa.pdf");
    private static final byte[] CONTENT_PDF = getBytesFromFile("/pdfs/navskjema.pdf");

    private final InnsendingTestDouble innsending = new InnsendingTestDouble();
    private final SkjemaOppslagServiceTestDouble skjemaOppslagService = new SkjemaOppslagServiceTestDouble();
    private final BrukernotifikasjonTestDouble brukernotifikasjon = new BrukernotifikasjonTestDouble();

    private final InnsendingService innsendingService = new InnsendingService(skjemaOppslagService, innsending, brukernotifikasjon, mock(SoknadService.class));

    @Test
    public void testSoknadsdataHasExpectedAttributes() {
        new TestRunner()
                .assertSoknadsdata(new Soknadsdata(BEHANDLINGSID, SKJEMANUMMER, true, AKTORID, TEMA, TITTEL))
                .testAndVerify();
    }

    @Test
    public void testHovedSkjemadataHasExpectedAttributes() {
        String fullSoknadId = UUID.randomUUID().toString();

        new TestRunner()
                .withMainPdf(CONTENT_PDF)
                .withFullversjonPdf(null)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"))
                .testAndVerify();

        new TestRunner()
                .withMainPdf(CONTENT_PDF)
                .withFullversjonPdf(CONTENT_UNKNOWN)
                .withFullversjonId(fullSoknadId)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"))
                .assertHovedskjemadata(1, new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .testAndVerify();

        new TestRunner()
                .withMainPdf(CONTENT_PDFA)
                .withFullversjonPdf(CONTENT_UNKNOWN)
                .withFullversjonId(fullSoknadId)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .assertHovedskjemadata(1, new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .testAndVerify();

        new TestRunner()
                .withMainPdf(CONTENT_UNKNOWN)
                .withFullversjonPdf(CONTENT_PDF)
                .withFullversjonId(fullSoknadId)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", DEFAULT_FILE_TYPE, SKJEMANUMMER + "." + DEFAULT_FILE_TYPE.toLowerCase()))
                .assertHovedskjemadata(1, new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .testAndVerify();

        new TestRunner()
                .withMainPdf(CONTENT_UNKNOWN)
                .withFullversjonPdf(CONTENT_PDFA)
                .withFullversjonId(fullSoknadId)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", DEFAULT_FILE_TYPE, SKJEMANUMMER + "." + DEFAULT_FILE_TYPE.toLowerCase()))
                .assertHovedskjemadata(1, new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .testAndVerify();
    }

    @Test
    public void testAlternativeRepresentations() {
        String fullSoknadId = UUID.randomUUID().toString();
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

        new TestRunner()
                .withMainPdf(CONTENT_PDF)
                .withFullversjonPdf(CONTENT_UNKNOWN)
                .withFullversjonId(fullSoknadId)
                .withAlternativeRepresentations(alternativeRepresentations)
                .assertHovedskjemadata(0, new Hovedskjemadata(ID_HOVEDSKJEMA, "application/pdf", "PDF", SKJEMANUMMER + ".pdf"))
                .assertHovedskjemadata(1, new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", "PDF/A", SKJEMANUMMER + ".pdfa"))
                .assertHovedskjemadata(2, new Hovedskjemadata("altRepId0", APPLICATION_JSON_VALUE, "JSON", "tiltakspenger.json"))
                .assertHovedskjemadata(3, new Hovedskjemadata("altRepId1", APPLICATION_XML_VALUE, "XML", "Tilleggsstonader.xml"))
                .assertHovedskjemadata(4, new Hovedskjemadata("altRepId2", "application/pdfa", "PDF", "apa.pdfa"))
                .assertHovedskjemadata(5, new Hovedskjemadata("altRepId3", "application/pdf", "PDF", "bepa.pdf"))
                .assertHovedskjemadata(6, new Hovedskjemadata("altRepId4", "made up mimetype for testing", DEFAULT_FILE_TYPE, "cepa.bmp"))
                .assertHovedskjemadata(7, new Hovedskjemadata("altRepId5", APPLICATION_JSON_VALUE, "JSON", SKJEMANUMMER + ".json"))
                .assertHovedskjemadata(8, new Hovedskjemadata("altRepId6", APPLICATION_XML_VALUE, "XML", SKJEMANUMMER + ".xml"))
                .testAndVerify();
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


        new TestRunner()
                .withVedlegg(vedlegg)
                .assertVedleggsdata(0, new Vedleggsdata("N6 with name", DEFAULT_VEDLEGG_MIMETYPE, "PDF", vedleggNavn, "N6", vedleggNavn))
                .assertVedleggsdata(1, new Vedleggsdata("N6 with name = null, skjemanummerTillegg = null", "application/json", "JSON", "jollyjson.json", "N6", TITTEL))
                .assertVedleggsdata(2, new Vedleggsdata("N6 with blank name, blank skjemanummerTillegg", DEFAULT_VEDLEGG_MIMETYPE, "PDF", DEFAULT_VEDLEGG_NAME, "N6", TITTEL))
                .assertVedleggsdata(3, new Vedleggsdata("L8", DEFAULT_VEDLEGG_MIMETYPE, "PDF", "L8", "L8", TITTEL + ": Apa Bepa"))
                .testAndVerify();
    }

    @Test
    public void testIfSkjemaOppslagServiceThrowsException_EmptyTitleIsSet() {
        String vedleggId = "L8";
        skjemaOppslagService.mockThatExceptionIsThrownOnArgument(vedleggId);
        List<Vedlegg> vedlegg = Collections.singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer(vedleggId)
                        .medFillagerReferanse(vedleggId)
                        .medStorrelse(71L));
        WebSoknad webSoknad = createWebSoknad(vedlegg);

        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, CONTENT_UNKNOWN, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        assertEquals(vedlegg.size(), innsending.observedVedleggsdata.size());
        assertEquals("", innsending.observedVedleggsdata.get(0).getTittel());
    }

    @Test
    public void testVedleggFileName() {
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
        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, CONTENT_UNKNOWN, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        assertEquals(vedlegg.size(), innsending.observedVedleggsdata.size());
        assertEquals("Apa", innsending.observedVedleggsdata.get(0).getFilename());
        assertEquals("Bepa", innsending.observedVedleggsdata.get(1).getFilename());
        assertEquals("Cepa", innsending.observedVedleggsdata.get(2).getFilename());
        assertEquals("Depa", innsending.observedVedleggsdata.get(3).getFilename());
        assertEquals(DEFAULT_VEDLEGG_NAME, innsending.observedVedleggsdata.get(4).getFilename());
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
        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, CONTENT_UNKNOWN, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        assertEquals(1, innsending.observedVedleggsdata.size());
        assertEquals("Vedlegg er LastetOpp", innsending.observedVedleggsdata.get(0).getId());
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
        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, CONTENT_UNKNOWN, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        assertEquals(1, innsending.observedVedleggsdata.size());
        assertEquals("vedleggWithSize71", innsending.observedVedleggsdata.get(0).getTittel());
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
        return new WebSoknad().medId(1L)
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
        try (InputStream resourceAsStream = InnsendingServiceTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private class TestRunner {
        private byte[] mainPdf = CONTENT_PDF;
        private byte[] fullversjonPdf = CONTENT_PDF;
        private String fullversjonId = UUID.randomUUID().toString();
        private List<AlternativRepresentasjon> alternativeRepresentations = emptyList();
        private List<Vedlegg> vedlegg = emptyList();
        private final Map<Integer, Hovedskjemadata> hovedskjemadataAssertions = new HashMap<>();
        private final Map<Integer, Vedleggsdata> vedleggAssertions = new HashMap<>();
        private Soknadsdata expectedSoknadsdata;


        public TestRunner withMainPdf(byte[] pdf) {
            mainPdf = pdf;
            return this;
        }

        public TestRunner withFullversjonPdf(byte[] pdf) {
            fullversjonPdf = pdf;
            return this;
        }

        public TestRunner withFullversjonId(String id) {
            fullversjonId = id;
            return this;
        }

        public TestRunner withAlternativeRepresentations(List<AlternativRepresentasjon> alternativeRepresentations) {
            this.alternativeRepresentations = alternativeRepresentations;
            return this;
        }

        public TestRunner withVedlegg(List<Vedlegg> vedlegg) {
            this.vedlegg = vedlegg;
            return this;
        }

        public TestRunner assertSoknadsdata(Soknadsdata soknadsdata) {
            expectedSoknadsdata = soknadsdata;
            return this;
        }

        public TestRunner assertHovedskjemadata(int index, Hovedskjemadata hovedskjemadata) {
            hovedskjemadataAssertions.put(index, hovedskjemadata);
            return this;
        }

        public TestRunner assertVedleggsdata(int index, Vedleggsdata vedleggsdata) {
            vedleggAssertions.put(index, vedleggsdata);
            return this;
        }

        public void testAndVerify() {
            WebSoknad webSoknad = createWebSoknad(vedlegg);

            innsendingService.sendSoknad(webSoknad, alternativeRepresentations, vedlegg, mainPdf, fullversjonPdf, fullversjonId);


            assertTrue(innsending.sendInnMethodWasCalled());
            assertEquals(vedleggAssertions.size(), innsending.observedVedleggsdata.size());
            int expectedNumberOfHovedskjemas = hovedskjemadataAssertions.isEmpty() ? 1 + (fullversjonPdf == null ? 0 : 1) : hovedskjemadataAssertions.size();
            assertEquals(expectedNumberOfHovedskjemas, innsending.observedHovedskjemadata.size());

            verifySoknadsdata();
            for (var entry : hovedskjemadataAssertions.entrySet()) {
                InnsendingServiceTest.this.assertHovedskjemadata(entry.getValue(), innsending.observedHovedskjemadata.get(entry.getKey()));
            }

            for (var entry : vedleggAssertions.entrySet()) {
                InnsendingServiceTest.this.assertVedleggsdata(entry.getValue(), innsending.observedVedleggsdata.get(entry.getKey()));
            }

            innsending.reset();
        }

        private void verifySoknadsdata() {
            if (expectedSoknadsdata != null) {
                Soknadsdata actual = innsending.observedSoknadsdata;
                assertEquals(expectedSoknadsdata.getTema(), actual.getTema());
                assertEquals(expectedSoknadsdata.getTittel(), actual.getTittel());
                assertEquals(expectedSoknadsdata.getAktoerId(), actual.getAktoerId());
                assertEquals(expectedSoknadsdata.getSkjemanummer(), actual.getSkjemanummer());
                assertEquals(expectedSoknadsdata.getBehandlingId(), actual.getBehandlingId());
                assertEquals(expectedSoknadsdata.getErEttersending(), actual.getErEttersending());
            }
        }
    }


    private static class InnsendingTestDouble implements Innsending {
        private Soknadsdata observedSoknadsdata = null;
        private List<Vedleggsdata> observedVedleggsdata = null;
        private List<Hovedskjemadata> observedHovedskjemadata = null;

        @Override
        public void sendInn(
                @NotNull Soknadsdata soknadsdata,
                @NotNull Collection<Vedleggsdata> vedleggsdata,
                @NotNull Collection<Hovedskjemadata> hovedskjemas
        ) {
            observedSoknadsdata = soknadsdata;
            observedVedleggsdata = new ArrayList<>(vedleggsdata);
            observedHovedskjemadata = new ArrayList<>(hovedskjemas);
        }


        public boolean sendInnMethodWasCalled() {
            return observedSoknadsdata != null;
        }

        public void reset() {
            observedSoknadsdata = null;
            observedVedleggsdata = null;
            observedHovedskjemadata = null;
        }
    }

    private static class SkjemaOppslagServiceTestDouble extends SkjemaOppslagService {
        private String exceptionThrowingArgument = null;

        @Override
        public String getTittel(String skjemanummer) {
            if (skjemanummer.equals(exceptionThrowingArgument))
                throw new RuntimeException("Mocked exception");
            return TITTEL;
        }

        @Override
        public String getTema(String skjemanummer) {
            if (skjemanummer.equals(exceptionThrowingArgument))
                throw new RuntimeException("Mocked exception");
            return TEMA;
        }


        public void mockThatExceptionIsThrownOnArgument(String argument) {
            exceptionThrowingArgument = argument;
        }
    }

    private static class BrukernotifikasjonTestDouble implements Brukernotifikasjon {
        private int callsToNewNotification = 0;

        @Override
        public void newNotification(@NotNull String skjemanavn, @NotNull String behandlingsId, @NotNull String behandlingskjedeId, boolean erEttersendelse, @NotNull String personId) {
            callsToNewNotification++;
        }

        @Override
        public void cancelNotification(@NotNull String behandlingsId, @NotNull String behandlingskjedeId, boolean erEttersendelse, @NotNull String personId) {
//            throw new RuntimeException("Mocked exception");
        }


        public int getCallsToNewNotificationAndReset() {
            int calls = callsToNewNotification;
            callsToNewNotification = 0;
            return calls;
        }
    }
}
