package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.sbl.soknadinnsending.innsending.SoknadDtoCreatorKt.createSoknad;
import static org.junit.jupiter.api.Assertions.*;

public class InnsendingServiceTest {

    private static final String TEMA = "TEMA";
    private static final String TITTEL = "TITTEL";

    private final InnsendingTestDouble innsending = new InnsendingTestDouble();
    private final SkjemaOppslagServiceTestDouble skjemaOppslagService = new SkjemaOppslagServiceTestDouble();

    private final InnsendingService innsendingService = new InnsendingService(skjemaOppslagService, innsending);


    @Test
    public void testPropertiesAndDtoConversion() throws IOException {
        String vedleggNavn = "vedleggNavn";
        String aktorId = "123456";
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name")
                        .medNavn(vedleggNavn),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with name = null, skjemanummerTillegg = null")
                        .medNavn(null)
                        .medSkjemanummerTillegg(null),
                new Vedlegg()
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6 with blank name, blank skjemanummerTillegg")
                        .medNavn("")
                        .medSkjemanummerTillegg(""),
                new Vedlegg()
                        .medSkjemaNummer("L8")
                        .medFillagerReferanse("L8")
                        .medSkjemanummerTillegg("Apa Bepa"));

        WebSoknad webSoknad = new WebSoknad().medId(1L)
                .medAktorId(aktorId)
                .medBehandlingId("123")
                .medUuid("uidHovedskjema")
                .medskjemaNummer("NAV 11-13.05")
                .medFaktum(new Faktum().medKey("personalia"))
                .medJournalforendeEnhet("enhet")
                .medVedlegg(vedlegg);

        innsendingService.sendSoknad(webSoknad, vedlegg, getBytesFromFile("/pdfs/navskjema.pdf"), new byte[]{4,5,6});

        Soknad dto;
        assertTrue(innsending.archiveMethodWasCalled());
        dto = innsending.lastArgumentToArchiveMethod;
        assertEquals(TEMA, dto.getTema());
        assertEquals(aktorId, dto.getPersonId());
        assertFalse(dto.getErEttersendelse());
        assertEquals(5, dto.getDokumenter().size());
        assertEquals(2, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(1).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(2).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(3).getVarianter().size());
        assertEquals(1, dto.getDokumenter().get(4).getVarianter().size());
        assertEquals("PDF", dto.getDokumenter().get(0).getVarianter().get(0).getFiltype());
        assertEquals("UNKNOWN", dto.getDokumenter().get(0).getVarianter().get(1).getFiltype());

        assertEquals("N6 with name", dto.getDokumenter().get(1).getVarianter().get(0).getId());
        assertEquals(vedleggNavn, dto.getDokumenter().get(1).getTittel());
        assertEquals("N6 with name = null, skjemanummerTillegg = null", dto.getDokumenter().get(2).getVarianter().get(0).getId());
        assertEquals(TITTEL, dto.getDokumenter().get(2).getTittel());
        assertEquals("N6 with blank name, blank skjemanummerTillegg", dto.getDokumenter().get(3).getVarianter().get(0).getId());
        assertEquals(TITTEL, dto.getDokumenter().get(3).getTittel());
        assertEquals("L8", dto.getDokumenter().get(4).getVarianter().get(0).getId());
        assertEquals(TITTEL + ": Apa Bepa", dto.getDokumenter().get(4).getTittel());

        innsending.reset();
        skjemaOppslagService.mockThatExceptionIsThrownOnArgument("L8");

        innsendingService.sendSoknad(webSoknad, vedlegg, getBytesFromFile("/pdfs/ceh.pdf"), null);

        assertTrue(innsending.archiveMethodWasCalled());
        dto = innsending.lastArgumentToArchiveMethod;
        assertEquals(5, dto.getDokumenter().size());
        assertEquals(1, dto.getDokumenter().get(0).getVarianter().size());
        assertEquals("PDF", dto.getDokumenter().get(0).getVarianter().get(0).getFiltype());

        assertEquals("L8", dto.getDokumenter().get(4).getVarianter().get(0).getId());
        assertEquals("", dto.getDokumenter().get(4).getTittel());
    }


    private static byte[] getBytesFromFile(String path) throws IOException {
        try (InputStream resourceAsStream = SoknadDataFletterTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        }
    }


    private static class InnsendingTestDouble implements Innsending {
        private Soknad lastArgumentToArchiveMethod = null;

        @Override
        public void sendInn(
                @NotNull Soknadsdata soknadsdata,
                @NotNull Collection<Vedleggsdata> vedleggsdata,
                @NotNull Collection<Hovedskjemadata> hovedskjemas
        ) {
            lastArgumentToArchiveMethod = createSoknad(soknadsdata, vedleggsdata, hovedskjemas);
        }


        public boolean archiveMethodWasCalled() {
            return lastArgumentToArchiveMethod != null;
        }

        public void reset() {
            lastArgumentToArchiveMethod = null;
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
