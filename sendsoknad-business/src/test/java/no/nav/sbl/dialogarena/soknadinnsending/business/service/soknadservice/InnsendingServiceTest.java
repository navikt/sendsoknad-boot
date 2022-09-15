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
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad;
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter;
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
import static no.nav.sbl.soknadinnsending.innsending.SoknadDtoCreatorKt.createSoknad;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;

public class InnsendingServiceTest {

    private static final String TEMA = "TEMA";
    private static final String TITTEL = "TITTEL";
    private static final String SKJEMANUMMER = "NAV 11-13.05";

    private static final byte[] CONTENT_PDFA = getBytesFromFile("/pdfs/pdfa.pdf");
    private static final byte[] CONTENT_PDF = getBytesFromFile("/pdfs/navskjema.pdf");

    private final InnsendingTestDouble innsending = new InnsendingTestDouble();
    private final SkjemaOppslagServiceTestDouble skjemaOppslagService = new SkjemaOppslagServiceTestDouble();
    private final BrukernotifikasjonTestDouble brukernotifikasjon = new BrukernotifikasjonTestDouble();

    private final InnsendingService innsendingService = new InnsendingService(skjemaOppslagService, innsending, brukernotifikasjon);

    @Test
    public void testSoknadHasExpectedAttributes() {
        String aktorId = "123456";
        byte[] mainPdfContent = {4, 5, 6};
        WebSoknad webSoknad = createWebSoknad(aktorId, emptyList());

        innsendingService.sendSoknad(webSoknad, emptyList(), emptyList(), mainPdfContent, CONTENT_PDFA, "fullversjonId");

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;

        assertEquals(1, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size()); // Hovedskjema + Fullversjon
        assertEquals(TEMA, dto.getTema());
        assertEquals(aktorId, dto.getPersonId());
        assertEquals("123", dto.getInnsendingId());
        assertTrue(dto.getErEttersendelse());
    }

    @Test
    public void testHovedSkjemadataHasExpectedAttributes() {
        Varianter expectedVariant;
        byte[] unknownContent = {4, 5, 6};
        String fullSoknadId = UUID.randomUUID().toString();
        WebSoknad webSoknad = createWebSoknad(emptyList());

        innsendingService.sendSoknad(webSoknad, emptyList(), emptyList(), unknownContent, CONTENT_PDFA, fullSoknadId);

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;

        assertEquals(1, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size()); // Hovedskjema + Fullversjon

        expectedVariant = new Varianter("idHovedskjema", "application/pdf", SKJEMANUMMER + "." + DEFAULT_FILE_TYPE.toLowerCase(), DEFAULT_FILE_TYPE);
        assertVariant(expectedVariant, actualVariant(0, 0));

        expectedVariant = new Varianter(fullSoknadId, "application/pdf-fullversjon", SKJEMANUMMER + ".pdfa", "PDF/A");
        assertVariant(expectedVariant, actualVariant(0, 1));


        innsendingService.sendSoknad(webSoknad, emptyList(), emptyList(), CONTENT_PDFA, unknownContent, fullSoknadId);

        expectedVariant = new Varianter("idHovedskjema", "application/pdf", SKJEMANUMMER + ".pdfa", "PDF/A");
        assertVariant(expectedVariant, actualVariant(0, 0));


        innsendingService.sendSoknad(webSoknad, emptyList(), emptyList(), CONTENT_PDF, unknownContent, fullSoknadId);

        expectedVariant = new Varianter("idHovedskjema", "application/pdf", SKJEMANUMMER + ".pdf", "PDF");
        assertVariant(expectedVariant, actualVariant(0, 0));
    }

    @Test
    public void testAlternativeRepresentations() {
        Varianter expectedVariant;
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


        innsendingService.sendSoknad(webSoknad, alternativeRepresentations, emptyList(), CONTENT_PDF, new byte[]{4, 5, 6}, fullSoknadId);

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(1, dto.getDokumenter().size());
        assertEquals(2 + alternativeRepresentations.size(), dto.getDokumenter().get(0).getVarianter().size());

        expectedVariant = new Varianter("idHovedskjema", "application/pdf", SKJEMANUMMER + ".pdf", "PDF");
        assertVariant(expectedVariant, actualVariant(0, 0));

        expectedVariant = new Varianter(fullSoknadId, "application/pdf-fullversjon", SKJEMANUMMER + ".pdfa", "PDF/A");
        assertVariant(expectedVariant, actualVariant(0, 1));

        expectedVariant = new Varianter("altRepId0", APPLICATION_JSON_VALUE, "tiltakspenger.json", "JSON");
        assertVariant(expectedVariant, actualVariant(0, 2));

        expectedVariant = new Varianter("altRepId1", APPLICATION_XML_VALUE, "Tilleggsstonader.xml", "XML");
        assertVariant(expectedVariant, actualVariant(0, 3));

        expectedVariant = new Varianter("altRepId2", "application/pdfa", "apa.pdfa", "PDF");
        assertVariant(expectedVariant, actualVariant(0, 4));

        expectedVariant = new Varianter("altRepId3", "application/pdf", "bepa.pdf", "PDF");
        assertVariant(expectedVariant, actualVariant(0, 5));

        expectedVariant = new Varianter("altRepId4", "made up mimetype for testing", "cepa.bmp", DEFAULT_FILE_TYPE);
        assertVariant(expectedVariant, actualVariant(0, 6));

        expectedVariant = new Varianter("altRepId5", APPLICATION_JSON_VALUE, SKJEMANUMMER + ".json", "JSON");
        assertVariant(expectedVariant, actualVariant(0, 7));

        expectedVariant = new Varianter("altRepId6", APPLICATION_XML_VALUE, SKJEMANUMMER + ".xml", "XML");
        assertVariant(expectedVariant, actualVariant(0, 8));
    }

    @Test
    public void testVedlegg() {
        Varianter expectedVariant;
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

        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(1 + vedlegg.size(), dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size()); // Hovedskjema + Fullversjon
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size()); // Vedlegg
        assertEquals(1, dto.getDokumenter().get(2).getVarianter().size()); // Vedlegg
        assertEquals(1, dto.getDokumenter().get(3).getVarianter().size()); // Vedlegg
        assertEquals(1, dto.getDokumenter().get(4).getVarianter().size()); // Vedlegg

        expectedVariant = new Varianter("N6 with name", DEFAULT_VEDLEGG_MIMETYPE, vedleggNavn, "PDF");
        assertVariant(expectedVariant, actualVariant(1, 0));
        assertEquals(vedleggNavn, dto.getDokumenter().get(1).getTittel());

        expectedVariant = new Varianter("N6 with name = null, skjemanummerTillegg = null", "application/json", "jollyjson.json", "JSON");
        assertVariant(expectedVariant, actualVariant(2, 0));
        assertEquals(TITTEL, dto.getDokumenter().get(2).getTittel());

        expectedVariant = new Varianter("N6 with blank name, blank skjemanummerTillegg", DEFAULT_VEDLEGG_MIMETYPE, DEFAULT_VEDLEGG_NAME, "PDF");
        assertVariant(expectedVariant, actualVariant(3, 0));
        assertEquals(TITTEL, dto.getDokumenter().get(3).getTittel());

        expectedVariant = new Varianter("L8", DEFAULT_VEDLEGG_MIMETYPE, "L8", "PDF");
        assertVariant(expectedVariant, actualVariant(4, 0));
        assertEquals(TITTEL + ": Apa Bepa", dto.getDokumenter().get(4).getTittel());
    }

    @Test
    public void unknownContentAsPdf_DefaultFileTypeIsSet() {
        byte[] unknownContent = {4, 5, 6};
        WebSoknad webSoknad = createWebSoknad(emptyList());

        innsendingService.sendSoknad(webSoknad, emptyList(), emptyList(), unknownContent, CONTENT_PDF, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(1, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size()); // Hovedskjema + Fullversjon
        assertEquals(DEFAULT_FILE_TYPE, actualVariant(0, 0).getFiltype());
    }

    @Test
    public void skjemaOppslagServiceThrowsException_EmptyTitleIsSet() {
        String vedleggId = "L8";
        skjemaOppslagService.mockThatExceptionIsThrownOnArgument(vedleggId);
        List<Vedlegg> vedlegg = Collections.singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer(vedleggId)
                        .medFillagerReferanse(vedleggId)
                        .medStorrelse(71L));

        WebSoknad webSoknad = createWebSoknad(vedlegg);

        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, null, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(1 + vedlegg.size(), dto.getDokumenter().size());
        assertEquals(1, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals("PDF", actualVariant(0, 0).getFiltype());

        assertEquals(vedleggId, actualVariant(1, 0).getId());
        assertEquals("", dto.getDokumenter().get(1).getTittel());
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


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(1 + vedlegg.size(), dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size()); // Hovedskjema + Fullversjon
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(2).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(3).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(4).getVarianter().size());
        assertEquals(SKJEMANUMMER + ".pdf", actualVariant(0, 0).getFilnavn());
        assertEquals(SKJEMANUMMER + ".pdfa", actualVariant(0, 1).getFilnavn());
        assertEquals("Apa", actualVariant(1, 0).getFilnavn());
        assertEquals("Bepa", actualVariant(2, 0).getFilnavn());
        assertEquals("Cepa", actualVariant(3, 0).getFilnavn());
        assertEquals("Depa", actualVariant(4, 0).getFilnavn());
        assertEquals(DEFAULT_VEDLEGG_NAME, actualVariant(5, 0).getFilnavn());
    }

    @Test
    public void testOnlyOpplastedeVedleggAreKept() {
        Varianter expectedVariant;
        List<Vedlegg> vedlegg = Arrays.stream(Vedlegg.Status.values())
                .map(status -> new Vedlegg()
                        .medInnsendingsvalg(status)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("Vedlegg er " + status)
                        .medNavn("name_" + status)
                        .medStorrelse(71L))
                .collect(Collectors.toList());

        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(2, dto.getDokumenter().size()); // Hovedskjema + Fullversjon
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());

        expectedVariant = new Varianter("Vedlegg er LastetOpp", DEFAULT_VEDLEGG_MIMETYPE, "name_LastetOpp", "PDF");
        assertVariant(expectedVariant, actualVariant(1, 0));
    }

    @Test
    public void testOnlyVedleggWithSizeAreKept() {
        Varianter expectedVariant;
        List<Vedlegg> vedlegg = List.of(
                new Vedlegg()
                        .medStorrelse(null)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId0")
                        .medNavn("vedleggNavn0"),
                new Vedlegg()
                        .medStorrelse(0L)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId1")
                        .medNavn("vedleggNavn1"),
                new Vedlegg()
                        .medStorrelse(71L)
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId2")
                        .medNavn("vedleggNavn2"));

        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(2, dto.getDokumenter().size()); // Hovedskjema + Fullversjon
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());

        expectedVariant = new Varianter("vedleggId2", DEFAULT_VEDLEGG_MIMETYPE, "vedleggNavn2", "PDF");
        assertVariant(expectedVariant, actualVariant(1, 0));
    }


    private Varianter actualVariant(int documentIndex, int variantIndex) {
        return innsending.lastArgumentToSendInnMethod.getDokumenter().get(documentIndex).getVarianter().get(variantIndex);
    }

    private void assertVariant(Varianter expectedVariant, Varianter actualVariant) {
        assertEquals(expectedVariant.getId(), actualVariant.getId());
        assertEquals(expectedVariant.getMediaType(), actualVariant.getMediaType());
        assertEquals(expectedVariant.getFilnavn(), actualVariant.getFilnavn());
        assertEquals(expectedVariant.getFiltype(), actualVariant.getFiltype());
    }

    private WebSoknad createWebSoknad(List<Vedlegg> vedlegg) {
        return createWebSoknad("123456", vedlegg);
    }

    private WebSoknad createWebSoknad(String aktorId, List<Vedlegg> vedlegg) {
        return new WebSoknad().medId(1L)
                .medAktorId(aktorId)
                .medBehandlingId("123")
                .medBehandlingskjedeId("68")
                .medUuid("idHovedskjema")
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


    private static class InnsendingTestDouble implements Innsending {
        private Soknad lastArgumentToSendInnMethod = null;

        @Override
        public void sendInn(
                @NotNull Soknadsdata soknadsdata,
                @NotNull Collection<Vedleggsdata> vedleggsdata,
                @NotNull Collection<Hovedskjemadata> hovedskjemas
        ) {
            lastArgumentToSendInnMethod = createSoknad(soknadsdata, vedleggsdata, hovedskjemas);
        }


        public boolean sendInnMethodWasCalled() {
            return lastArgumentToSendInnMethod != null;
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
            throw new RuntimeException("Mocked exception");
        }


        public int getCallsToNewNotificationAndReset() {
            int calls = callsToNewNotification;
            callsToNewNotification = 0;
            return calls;
        }
    }
}
