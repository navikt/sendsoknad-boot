package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.SoknadInnsendtDto;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class InnsendingOgOpplastingServiceTest {

    private static final String TEMA = "TEMA";
    private static final String TITTEL = "TITTEL";

    private final InnsendingTestDouble innsending = new InnsendingTestDouble();
    private final FilestorageTestDouble filestorage = new FilestorageTestDouble();
    private final SkjemaOppslagServiceTestDouble skjemaOppslagService = new SkjemaOppslagServiceTestDouble();

    private final InnsendingOgOpplastingService innsendingOgOpplastingService = new InnsendingOgOpplastingService(skjemaOppslagService, innsending, filestorage);


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

        innsendingOgOpplastingService.sendSoknad(webSoknad, vedlegg, getBytesFromFile("/pdfs/navskjema.pdf"), new byte[]{4,5,6});

        SoknadInnsendtDto dto;
        assertTrue(innsending.archiveMethodWasCalled());
        assertTrue(filestorage.storeMethodWasCalled());
        assertEquals(2, filestorage.lastArgumentToStoreMethod.size());
        dto = innsending.lastArgumentToArchiveMethod;
        assertEquals(TEMA, dto.getTema());
        assertEquals(aktorId, dto.getPersonId());
        assertFalse(dto.getEttersendelse());
        assertEquals(5, dto.getInnsendteDokumenter().length);
        assertEquals(2, dto.getInnsendteDokumenter()[0].getVarianter().length);
        assertEquals(1, dto.getInnsendteDokumenter()[1].getVarianter().length);
        assertEquals(1, dto.getInnsendteDokumenter()[2].getVarianter().length);
        assertEquals(1, dto.getInnsendteDokumenter()[3].getVarianter().length);
        assertEquals(1, dto.getInnsendteDokumenter()[4].getVarianter().length);
        assertEquals("PDF", dto.getInnsendteDokumenter()[0].getVarianter()[0].getFiltype());
        assertEquals("UNKNOWN", dto.getInnsendteDokumenter()[0].getVarianter()[1].getFiltype());

        assertEquals("N6 with name", dto.getInnsendteDokumenter()[1].getVarianter()[0].getId());
        assertEquals(vedleggNavn, dto.getInnsendteDokumenter()[1].getTittel());
        assertEquals("N6 with name = null, skjemanummerTillegg = null", dto.getInnsendteDokumenter()[2].getVarianter()[0].getId());
        assertEquals(TITTEL, dto.getInnsendteDokumenter()[2].getTittel());
        assertEquals("N6 with blank name, blank skjemanummerTillegg", dto.getInnsendteDokumenter()[3].getVarianter()[0].getId());
        assertEquals(TITTEL, dto.getInnsendteDokumenter()[3].getTittel());
        assertEquals("L8", dto.getInnsendteDokumenter()[4].getVarianter()[0].getId());
        assertEquals(TITTEL + ": Apa Bepa", dto.getInnsendteDokumenter()[4].getTittel());

        innsending.reset();
        filestorage.reset();
        skjemaOppslagService.mockThatExceptionIsThrownOnArgument("L8");

        innsendingOgOpplastingService.sendSoknad(webSoknad, vedlegg, getBytesFromFile("/pdfs/ceh.pdf"), null);

        assertTrue(innsending.archiveMethodWasCalled());
        assertTrue(filestorage.storeMethodWasCalled());
        assertEquals(1, filestorage.lastArgumentToStoreMethod.size());
        dto = innsending.lastArgumentToArchiveMethod;
        assertEquals(5, dto.getInnsendteDokumenter().length);
        assertEquals(1, dto.getInnsendteDokumenter()[0].getVarianter().length);
        assertEquals("PDF", dto.getInnsendteDokumenter()[0].getVarianter()[0].getFiltype());

        assertEquals("L8", dto.getInnsendteDokumenter()[4].getVarianter()[0].getId());
        assertEquals("", dto.getInnsendteDokumenter()[4].getTittel());
    }


    private static byte[] getBytesFromFile(String path) throws IOException {
        try (InputStream resourceAsStream = SoknadDataFletterTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        }
    }


    private static class FilestorageTestDouble implements Filestorage {
        private List<FilElementDto> lastArgumentToStoreMethod = null;


        @Override
        public void store(@NotNull List<FilElementDto> filer) {
            lastArgumentToStoreMethod = filer;
        }

        @NotNull
        @Override
        public List<FilElementDto> get(@NotNull List<String> ids) {
            return emptyList();
        }

        @Override
        public void delete(@NotNull List<String> ids) { }


        public boolean storeMethodWasCalled() {
            return lastArgumentToStoreMethod != null;
        }

        public void reset() {
            lastArgumentToStoreMethod = null;
        }
    }

    private static class InnsendingTestDouble implements Innsending {
        private SoknadInnsendtDto lastArgumentToArchiveMethod = null;

        @Override
        public void sendInn(@NotNull SoknadInnsendtDto data) {
            lastArgumentToArchiveMethod = data;
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
