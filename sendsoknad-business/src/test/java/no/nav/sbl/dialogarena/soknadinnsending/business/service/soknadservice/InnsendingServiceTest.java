package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.brukernotifikasjon.Brukernotifikasjon;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.SendesSenere;
import static org.mockito.Mockito.*;

public class InnsendingServiceTest {

    private static final String BEHANDLINGSID = "123";
    private static final String AKTORID = "123456";
    private static final String ID_HOVEDSKJEMA = "idHovedskjema";
    private static final String SKJEMANUMMER = "NAV 11-12.10";

    private static final byte[] CONTENT_PDF = getBytesFromFile("/pdfs/navskjema.pdf");
    private static final byte[] CONTENT_PDFA = getBytesFromFile("/pdfs/pdfa.pdf");

    private final Innsending innsending = mock(Innsending.class);
    private final Brukernotifikasjon brukernotifikasjon = mock(Brukernotifikasjon.class);
    private final SoknadService soknadService = mock(SoknadService.class);

    private final InnsendingService innsendingService = new InnsendingService(innsending, brukernotifikasjon, soknadService);


    @BeforeAll
    public static void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
    }


    @Test
    public void testSendSoknad_noVedlegg_willCallSendInnAndCancelNotification() {
        innsendingService.sendSoknad(createWebSoknad(emptyList()), emptyList(), emptyList(), CONTENT_PDF, CONTENT_PDF, UUID.randomUUID().toString());

        verify(innsending, times(1)).sendInn(any(), any(), any());
        verify(brukernotifikasjon, times(1)).cancelNotification(eq(SKJEMANUMMER), eq(BEHANDLINGSID), eq(true), eq(AKTORID));
        verify(soknadService, never()).startEttersending(anyString(), anyString());
    }

    @Test
    public void testSendSoknad_AllVedleggAreLastetOpp_willNotStartEttersending() {
        List<Vedlegg> vedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("N6")
                        .medFillagerReferanse("N6")
                        .medStorrelse(71L),
                new Vedlegg()
                        .medInnsendingsvalg(LastetOpp)
                        .medSkjemaNummer("L8")
                        .medFillagerReferanse("L8")
                        .medStorrelse(71L));


        innsendingService.sendSoknad(createWebSoknad(vedlegg), emptyList(), vedlegg, CONTENT_PDF, CONTENT_PDF, UUID.randomUUID().toString());

        verify(soknadService, never()).startEttersending(anyString(), anyString());
    }

    @Test
    public void testSendSoknad_VedleggIsSendesSenareWithData_willNotStartEttersending() {
        List<Vedlegg> vedlegg = singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(SendesSenere)
                        .medData(CONTENT_PDFA)
                        .medFillagerReferanse("L8")
                        .medStorrelse(71L));


        innsendingService.sendSoknad(createWebSoknad(vedlegg), emptyList(), vedlegg, CONTENT_PDF, CONTENT_PDF, UUID.randomUUID().toString());

        verify(soknadService, never()).startEttersending(anyString(), anyString());
    }

    @Test
    public void testSendSoknad_VedleggIsSendesSenareWithoutData_willStartEttersending() {
        List<Vedlegg> vedlegg = singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(SendesSenere)
                        .medFillagerReferanse("L8")
                        .medStorrelse(71L));


        innsendingService.sendSoknad(createWebSoknad(vedlegg), emptyList(), vedlegg, CONTENT_PDF, CONTENT_PDF, UUID.randomUUID().toString());

        verify(soknadService, times(1)).startEttersending(anyString(), anyString());
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
}
