package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
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

    private final InnsendingService innsendingService = new InnsendingService(skjemaOppslagService, innsending);

    @Test
    public void testProperties() {
        String aktorId = "123456";
        byte[] unknownContent = {4, 5, 6};
        String fullSoknadId = UUID.randomUUID().toString();

        List<Vedlegg> vedlegg = List.of(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId")
                        .medNavn("vedleggNavn"));

        WebSoknad webSoknad = createWebSoknad(aktorId, vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, unknownContent, CONTENT_PDFA, fullSoknadId);

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(TEMA, dto.getTema());
        assertEquals(aktorId, dto.getPersonId());
        assertEquals("123", dto.getInnsendingId());
        assertTrue(dto.getErEttersendelse());
    }

    @Test
    public void testHovedSkjemadata() {
        Varianter variant;
        String aktorId = "123456";
        byte[] unknownContent = {4, 5, 6};
        String fullSoknadId = UUID.randomUUID().toString();

        List<Vedlegg> vedlegg = List.of(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId")
                        .medNavn("vedleggNavn"));

        WebSoknad webSoknad = createWebSoknad(aktorId, vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, unknownContent, CONTENT_PDFA, fullSoknadId);

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;

        assertEquals(2, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());

        variant = actualVariant(0, 0);
        assertEquals("idHovedskjema", variant.getId());
        assertEquals("application/pdf", variant.getMediaType());
        assertEquals(DEFAULT_FILE_TYPE.toUpperCase(), variant.getFiltype());
        assertEquals(SKJEMANUMMER + "." + DEFAULT_FILE_TYPE.toLowerCase(), variant.getFilnavn());

        variant = actualVariant(0, 1);
        assertEquals("PDF/A", variant.getFiltype());
        assertEquals("application/pdf-fullversjon", variant.getMediaType());
        assertEquals(SKJEMANUMMER + ".pdfa", variant.getFilnavn());
        assertEquals(fullSoknadId, variant.getId());


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDFA, unknownContent, fullSoknadId);

        variant = actualVariant(0, 0);
        assertEquals("idHovedskjema", variant.getId());
        assertEquals("application/pdf", variant.getMediaType());
        assertEquals("PDF/A", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".pdfa", variant.getFilnavn());


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, unknownContent, fullSoknadId);

        variant = actualVariant(0, 0);
        assertEquals("idHovedskjema", variant.getId());
        assertEquals("application/pdf", variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".pdf", variant.getFilnavn());
    }

    @Test
    public void testAlternativeRepresentations() {
        Varianter variant;
        String fullSoknadId = UUID.randomUUID().toString();
        List<Vedlegg> vedlegg = List.of(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("vedleggId")
                        .medNavn("vedleggNavn"));

        WebSoknad webSoknad = createWebSoknad(vedlegg);

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


        innsendingService.sendSoknad(webSoknad, alternativeRepresentations, vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, fullSoknadId);

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(2, dto.getDokumenter().size());
        assertEquals(2 + alternativeRepresentations.size(), dto.getDokumenter().get(0).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());

        variant = actualVariant(0, 0);
        assertEquals("idHovedskjema", variant.getId());
        assertEquals("application/pdf", variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".pdf", variant.getFilnavn());

        variant = actualVariant(0, 1);
        assertEquals(fullSoknadId, variant.getId());
        assertEquals("application/pdf-fullversjon", variant.getMediaType());
        assertEquals("PDF/A", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".pdfa", variant.getFilnavn());

        variant = actualVariant(0, 2);
        assertEquals("altRepId0", variant.getId());
        assertEquals(APPLICATION_JSON_VALUE, variant.getMediaType());
        assertEquals("JSON", variant.getFiltype());
        assertEquals("tiltakspenger.json", variant.getFilnavn());

        variant = actualVariant(0, 3);
        assertEquals("altRepId1", variant.getId());
        assertEquals(APPLICATION_XML_VALUE, variant.getMediaType());
        assertEquals("XML", variant.getFiltype());
        assertEquals("Tilleggsstonader.xml", variant.getFilnavn());

        variant = actualVariant(0, 4);
        assertEquals("altRepId2", variant.getId());
        assertEquals("application/pdfa", variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals("apa.pdfa", variant.getFilnavn());

        variant = actualVariant(0, 5);
        assertEquals("altRepId3", variant.getId());
        assertEquals("application/pdf", variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals("bepa.pdf", variant.getFilnavn());

        variant = actualVariant(0, 6);
        assertEquals("altRepId4", variant.getId());
        assertEquals("made up mimetype for testing", variant.getMediaType());
        assertEquals(DEFAULT_FILE_TYPE.toUpperCase(), variant.getFiltype());
        assertEquals("cepa.bmp", variant.getFilnavn());

        variant = actualVariant(0, 7);
        assertEquals("altRepId5", variant.getId());
        assertEquals(APPLICATION_JSON_VALUE, variant.getMediaType());
        assertEquals("JSON", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".json", variant.getFilnavn());

        variant = actualVariant(0, 8);
        assertEquals("altRepId6", variant.getId());
        assertEquals(APPLICATION_XML_VALUE, variant.getMediaType());
        assertEquals("XML", variant.getFiltype());
        assertEquals(SKJEMANUMMER + ".xml", variant.getFilnavn());
    }

    @Test
    public void testVedlegg() {
        Varianter variant;
        String vedleggNavn = "vedleggNavn";
        String aktorId = "123456";
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name")
                        .medNavn(vedleggNavn)
                        .medMimetype(""),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name = null, skjemanummerTillegg = null")
                        .medNavn(null)
                        .medSkjemanummerTillegg(null)
                        .medFilnavn("jollyjson.json")
                        .medMimetype("application/json"),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with blank name, blank skjemanummerTillegg")
                        .medNavn("")
                        .medSkjemanummerTillegg(""),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("L8")
                        .medFillagerReferanse("L8")
                        .medSkjemanummerTillegg("Apa Bepa"));

        WebSoknad webSoknad = createWebSoknad(aktorId, vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4,5,6}, UUID.randomUUID().toString());

        Soknad dto;
        assertTrue(innsending.sendInnMethodWasCalled());
        dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(5, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(2).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(3).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(4).getVarianter().size());

        variant = actualVariant(1, 0);
        assertEquals("N6 with name", variant.getId());
        assertEquals(DEFAULT_VEDLEGG_MIMETYPE, variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals(vedleggNavn, variant.getFilnavn());
        assertEquals(vedleggNavn, dto.getDokumenter().get(1).getTittel());

        variant = actualVariant(2, 0);
        assertEquals("N6 with name = null, skjemanummerTillegg = null", variant.getId());
        assertEquals("application/json", variant.getMediaType());
        assertEquals("JSON", variant.getFiltype());
        assertEquals("jollyjson.json", variant.getFilnavn());
        assertEquals(TITTEL, dto.getDokumenter().get(2).getTittel());

        variant = actualVariant(3, 0);
        assertEquals("N6 with blank name, blank skjemanummerTillegg", variant.getId());
        assertEquals(DEFAULT_VEDLEGG_MIMETYPE, variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals(DEFAULT_VEDLEGG_NAME, variant.getFilnavn());
        assertEquals(TITTEL, dto.getDokumenter().get(3).getTittel());

        variant = actualVariant(4, 0);
        assertEquals("L8", variant.getId());
        assertEquals(DEFAULT_VEDLEGG_MIMETYPE, variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals("L8", variant.getFilnavn());
        assertEquals(TITTEL + ": Apa Bepa", dto.getDokumenter().get(4).getTittel());


        innsending.reset();
        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, new byte[]{1, 2, 3}, null, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(5, dto.getDokumenter().size());
        assertEquals(1, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals(DEFAULT_FILE_TYPE.toUpperCase(), actualVariant(0, 0).getFiltype());


        innsending.reset();
        skjemaOppslagService.mockThatExceptionIsThrownOnArgument("L8");
        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, null, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(5, dto.getDokumenter().size());
        assertEquals(1, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals("PDF", actualVariant(0, 0).getFiltype());

        assertEquals("L8", actualVariant(4, 0).getId());
        assertEquals("", dto.getDokumenter().get(4).getTittel());
    }

    @Test
    public void testVedleggFileName() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with Name but null filename")
                        .medFilnavn(null)
                        .medNavn("Apa"),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with Name but empty filename")
                        .medFilnavn("")
                        .medNavn("Bepa"),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("Cepa")
                        .medFillagerReferanse("Cepa with Name but empty filename")
                        .medFilnavn("")
                        .medNavn("vedleggNavn"),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with filename")
                        .medFilnavn("Depa")
                        .medNavn("vedleggNavn"),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with empty filename and null name")
                        .medFilnavn("")
                        .medNavn(null));

        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        assertTrue(innsending.sendInnMethodWasCalled());
        Soknad dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(6, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size());
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
        Varianter variant;
        List<Vedlegg> vedlegg = Arrays.stream(Vedlegg.Status.values())
                .map(status -> new Vedlegg()
                            .medInnsendingsvalg(status)
                            .medSkjemaNummer("N6")
                            .medFillagerReferanse("Vedlegg er " + status)
                            .medNavn("name_" + status))
                .collect(Collectors.toList());

        WebSoknad webSoknad = createWebSoknad(vedlegg);


        innsendingService.sendSoknad(webSoknad, emptyList(), vedlegg, CONTENT_PDF, new byte[]{4, 5, 6}, UUID.randomUUID().toString());

        Soknad dto;
        assertTrue(innsending.sendInnMethodWasCalled());
        dto = innsending.lastArgumentToSendInnMethod;
        assertEquals(2, dto.getDokumenter().size());
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());

        variant = actualVariant(1, 0);
        assertEquals("Vedlegg er LastetOpp", variant.getId());
        assertEquals(DEFAULT_VEDLEGG_MIMETYPE, variant.getMediaType());
        assertEquals("PDF", variant.getFiltype());
        assertEquals("name_LastetOpp", variant.getFilnavn());
    }


    private Varianter actualVariant(int documentIndex, int variantIndex) {
        return innsending.lastArgumentToSendInnMethod.getDokumenter().get(documentIndex).getVarianter().get(variantIndex);
    }

    private WebSoknad createWebSoknad(List<Vedlegg> vedlegg) {
        return createWebSoknad("123456", vedlegg);
    }

    private WebSoknad createWebSoknad(String aktorId, List<Vedlegg> vedlegg) {
        return new WebSoknad().medId(1L)
                .medAktorId(aktorId)
                .medBehandlingId("123")
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

        public void reset() {
            lastArgumentToSendInnMethod = null;
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
}
