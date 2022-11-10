package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InnsendtSoknadServiceTest {

    private static final String HOVEDSKJEMA_NR = "NAV 11-12.12";
    private static final String HOVEDSKJEMA_TITTEL = "Apa Bepa";
    private static final String BEHANDLINGS_ID = "ID01";
    private static final String SPRAK = "no_NB";

    private final Vedlegg hovedVedlegg = new Vedlegg()
            .medVedleggId(1L)
            .medSoknadId(1L)
            .medTittel(HOVEDSKJEMA_TITTEL)
            .medNavn("Hovedskjema")
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medSkjemaNummer(HOVEDSKJEMA_NR);
    private final Vedlegg kvitteringsVedlegg = new Vedlegg()
            .medVedleggId(2L)
            .medSoknadId(1L)
            .medTittel("Kvittering")
            .medNavn("Kvittering")
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medSkjemaNummer(SKJEMANUMMER_KVITTERING);
    private final Vedlegg annetVedlegg = new Vedlegg()
            .medVedleggId(3L)
            .medSoknadId(1L)
            .medTittel("Noe annet")
            .medNavn("Noe annet")
            .medInnsendingsvalg(Vedlegg.Status.SendesSenere)
            .medSkjemaNummer("N6");
    private final Vedlegg avAndreVedlegg = new Vedlegg()
            .medVedleggId(4L)
            .medSoknadId(1L)
            .medTittel("C2 skjema")
            .medNavn("C2 skjema")
            .medInnsendingsvalg(Vedlegg.Status.VedleggSendesAvAndre)
            .medSkjemaNummer("C2");
    private final Vedlegg sendesIkkeVedlegg = new Vedlegg()
            .medVedleggId(5L)
            .medSoknadId(1L)
            .medTittel("Annet skjema")
            .medNavn("")
            .medInnsendingsvalg(Vedlegg.Status.SendesIkke)
            .medSkjemaNummer("N6");
    private final Vedlegg alleredeSendtVedlegg = new Vedlegg()
            .medVedleggId(5L)
            .medSoknadId(1L)
            .medTittel(null)
            .medNavn("X2 skjema")
            .medInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt)
            .medSkjemaNummer("X2");

    private final List<Vedlegg> vedleggsListe = asList(
            hovedVedlegg,
            kvitteringsVedlegg,
            sendesIkkeVedlegg,
            avAndreVedlegg,
            annetVedlegg,
            alleredeSendtVedlegg
    );


    @Mock
    private VedleggService vedleggService;

    @Mock
    private SoknadRepository lokalDb;

    @InjectMocks
    private InnsendtSoknadService service;


    @Before
    public void setup() throws IOException {
        SkjemaOppslagService.initializeFromOldResult();
    }


    @Test
    public void finnerIkkeVedlegg_KasterException() {
        mockDatabase(createSoknad().medVedlegg(emptyList()));

        assertThrows(SendSoknadException.class, () -> service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK));

        verify(vedleggService, times(1)).leggTilKodeverkFelter(any());
    }

    @Test
    public void finnerIkkeKonfigurasjon_LeserTemaFraSkjemaOppslagServiceOgSetterIkkeTittelCmsKey() {
        String skjemanummer = "NAV 04-03.07";
        String expectedTemaForSkjemanummer = "DAG"; // From the local sanity.json backup file
        mockDatabase(createSoknad().medskjemaNummer(skjemanummer));

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);

        assertEquals(expectedTemaForSkjemanummer, result.getTemakode());
        assertNull(result.getTittelCmsKey());
        verify(vedleggService, times(1)).leggTilKodeverkFelter(any());
    }

    @Test
    public void finnerKonfigurasjon_LeserTemaFraKonfigurasjonOgSetterTittelCmsKey() {
        String expectedTemaForSkjemanummer = "TSO";
        mockDatabase(createSoknad().medskjemaNummer(HOVEDSKJEMA_NR));

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);

        assertEquals(expectedTemaForSkjemanummer, result.getTemakode());
        assertEquals("soknadtilleggsstonader.skjema.tittel", result.getTittelCmsKey());
        verify(vedleggService, times(1)).leggTilKodeverkFelter(any());
    }

    @Test
    public void finnerIkkeHovedskjema_LeserTittelFraSkjemaOppslagService() {
        mockDatabase(
                createSoknad().medVedlegg(singletonList(annetVedlegg.medInnsendingsvalg(Vedlegg.Status.LastetOpp)))
        );

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);

        assertEquals("Søknad om tilleggsstønader", result.getTittel());
    }

    @Test
    public void henterTittelFraHovedskjema() {
        mockDatabase(createSoknad());

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);

        assertEquals(HOVEDSKJEMA_TITTEL, result.getTittel());
    }

    @Test
    public void setterVedleggKorrekt() {
        WebSoknad soknad = createSoknad();
        mockDatabase(soknad);

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);


        List<Vedlegg> expectedHovedvedlegg = singletonList(
                new Vedlegg()
                        .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                        .medSkjemaNummer(HOVEDSKJEMA_NR)
                        .medSkjemanummerTillegg(null)
                        .medTittel(HOVEDSKJEMA_TITTEL)
                        .medNavn("Hovedskjema")
                        .medFillagerReferanse(null)
        );

        List<Vedlegg> expectedIkkeInnsendteVedlegg = asList(
                new Vedlegg()
                        .medInnsendingsvalg(Vedlegg.Status.SendesIkke)
                        .medSkjemaNummer("N6")
                        .medSkjemanummerTillegg(null)
                        .medTittel("Annet skjema")
                        .medNavn("Annet")
                        .medFillagerReferanse(null),
                new Vedlegg()
                        .medInnsendingsvalg(Vedlegg.Status.VedleggSendesAvAndre)
                        .medSkjemaNummer("C2")
                        .medSkjemanummerTillegg(null)
                        .medTittel("C2 skjema")
                        .medNavn("C2 skjema")
                        .medFillagerReferanse(null),
                new Vedlegg()
                        .medInnsendingsvalg(Vedlegg.Status.SendesSenere)
                        .medSkjemaNummer("N6")
                        .medSkjemanummerTillegg(null)
                        .medTittel("Noe annet")
                        .medNavn("Noe annet")
                        .medFillagerReferanse(null),
                new Vedlegg()
                        .medInnsendingsvalg(Vedlegg.Status.VedleggAlleredeSendt)
                        .medSkjemaNummer("X2")
                        .medSkjemanummerTillegg(null)
                        .medTittel("Dokumentasjon av sosialhjelp")
                        .medNavn("X2 skjema")
                        .medFillagerReferanse(null)
        );

        assertEquals(expectedHovedvedlegg, removeFillagerReferanse(result.getInnsendteVedlegg()));
        assertEquals(expectedIkkeInnsendteVedlegg, removeFillagerReferanse(result.getIkkeInnsendteVedlegg()));
        assertThat(result.getIkkeInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
        assertThat(result.getInnsendteVedlegg()).areNot(liktSkjemanummer(SKJEMANUMMER_KVITTERING));
    }

    @Test
    public void setterRiktigeVerdier() {
        WebSoknad soknad = createSoknad();
        mockDatabase(soknad);

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, SPRAK);

        assertEquals(BEHANDLINGS_ID, result.getBehandlingsId());
        assertEquals("7. januar 2022", result.getDato());
        assertEquals("18.55", result.getKlokkeslett());
    }

    @Test
    public void handtererSprak() {
        WebSoknad soknad = createSoknad();
        mockDatabase(soknad);

        InnsendtSoknad result = service.hentInnsendtSoknad(BEHANDLINGS_ID, "en");

        assertEquals("7. January 2022", result.getDato());
        assertEquals("18.55", result.getKlokkeslett());
    }


    private List<Vedlegg> removeFillagerReferanse(List<Vedlegg> vedlegg) {
        return vedlegg.stream().map(v -> v.medFillagerReferanse(null)).collect(Collectors.toList());
    }

    private void mockDatabase(WebSoknad webSoknad) {
        when(lokalDb.hentSoknadMedVedlegg(anyString())).thenReturn(webSoknad);
    }


    @SuppressWarnings("SameParameterValue")
    private Condition<Vedlegg> liktSkjemanummer(final String skjemanummer) {
        return new Condition<>() {
            @Override
            public boolean matches(Vedlegg vedlegg) {
                return skjemanummer.equals(vedlegg.getSkjemaNummer());
            }
        };
    }

    private WebSoknad createSoknad() {
        return new WebSoknad()
                .medAktorId("1234")
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.of(2022, Month.JANUARY, 7, 18, 55, 17)))
                .medBehandlingId(BEHANDLINGS_ID)
                .medId(1)
                .medskjemaNummer(HOVEDSKJEMA_NR)
                .medVedlegg(vedleggsListe);
    }
}
