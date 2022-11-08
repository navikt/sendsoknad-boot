package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static org.mockito.Mockito.*;

public class InnsendingServiceTest {

    private static final String BEHANDLINGSID = "71";
    private static final String BEHANDLINGSKEDJEID = "68";
    private static final String AKTORID = "123456";
    private static final String ID_HOVEDSKJEMA = "idHovedskjema";
    private static final String SKJEMANUMMER = "NAV 11-12.10";

    private static final byte[] CONTENT_PDF = getBytesFromFile("/pdfs/navskjema.pdf");

    private final Innsending innsending = mock(Innsending.class);
    private final Brukernotifikasjon brukernotifikasjon = mock(Brukernotifikasjon.class);

    private final InnsendingService innsendingService = new InnsendingService(innsending, brukernotifikasjon);


    @BeforeAll
    public static void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
    }


    @Test
    public void testSendSoknad_noVedlegg_willCallSendInnAndCancelNotification() {
        innsendingService.sendSoknad(createWebSoknad(emptyList()), emptyList(), emptyList(), CONTENT_PDF, CONTENT_PDF, UUID.randomUUID().toString());

        verify(innsending, times(1)).sendInn(any(), any(), any());
        verify(brukernotifikasjon, times(1)).cancelNotification(eq(BEHANDLINGSID), eq(BEHANDLINGSKEDJEID), eq(true), eq(AKTORID));
    }


    private WebSoknad createWebSoknad(List<Vedlegg> vedlegg) {
        return new WebSoknad().medId(1L)
                .medAktorId(AKTORID)
                .medBehandlingId(BEHANDLINGSID)
                .medBehandlingskjedeId(BEHANDLINGSKEDJEID)
                .medUuid(ID_HOVEDSKJEMA)
                .medskjemaNummer(SKJEMANUMMER)
                .medFaktum(new Faktum().medKey("personalia"))
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medJournalforendeEnhet("enhet")
                .medVedlegg(vedlegg);
    }

    private static byte[] getBytesFromFile(@SuppressWarnings("SameParameterValue") String path) {
        try (InputStream resourceAsStream = InnsendingServiceTest.class.getResourceAsStream(path)) {
            return IOUtils.toByteArray(resourceAsStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
