package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggHentOgPersistService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EttersendingServiceTest {

    private static final String BEHANDLINGSID = "71";
    private static final String AKTORID = "68";
    private static final String SKJEMANUMMER = "NAV 11-12.12";
    private static final String JOURNALFORENDEENHET = "apa bepa";
    private static final Long SOKNADID = 63L;

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;
    @Mock
    private FaktaService faktaService;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private VedleggHentOgPersistService vedleggHentOgPersistService;
    @Mock
    private Brukernotifikasjon brukernotifikasjon;

    @InjectMocks
    private EttersendingService ettersendingService;


    @Test
    public void finnerIkkeSoknadIDb_KasterException() {
        when(lokalDb.hentNyesteSoknadGittBehandlingskjedeId(eq(BEHANDLINGSID))).thenReturn(null);

        assertThrows(SendSoknadException.class, () -> ettersendingService.start(BEHANDLINGSID, AKTORID));

        verify(lokalDb, never()).opprettSoknad(any());
        verify(faktaService, never()).lagreSystemFaktum(any(), any());
        verify(vedleggHentOgPersistService, never()).persisterVedlegg(anyString(), any());
        verify(soknadMetricsService, never()).startetSoknad(any(), anyBoolean());
        verify(brukernotifikasjon, never()).newNotification(any(), any(), any(), anyBoolean(), any());
    }

    @Test
    public void finnerSoknadIDb_EttersendingOpprettes() {
        WebSoknad soknad = WebSoknad.startSoknad()
                .medAktorId(AKTORID)
                .medskjemaNummer(SKJEMANUMMER)
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()));
        when(lokalDb.hentNyesteSoknadGittBehandlingskjedeId(eq(BEHANDLINGSID))).thenReturn(soknad);
        when(lokalDb.opprettSoknad(any())).thenReturn(SOKNADID);

        String nyBehandlingsId = ettersendingService.start(BEHANDLINGSID, AKTORID);

        verify(lokalDb, times(1)).opprettSoknad(any());
        verify(faktaService, times(1)).lagreSystemFaktum(eq(SOKNADID), eq(expectedFaktum(soknad)));
        verify(vedleggHentOgPersistService, times(1)).persisterVedlegg(eq(nyBehandlingsId), eq(emptyList()));
        verify(soknadMetricsService, times(1)).startetSoknad(eq(SKJEMANUMMER), eq(true));
        verify(brukernotifikasjon, times(1))
                .newNotification(eq(SKJEMANUMMER), eq(nyBehandlingsId), eq(BEHANDLINGSID), eq(true), eq(AKTORID));
    }

    @Test
    public void finnerSoknadIDb_EttersendingOpprettesMedRiktigeVerdier() {
        WebSoknad soknad = WebSoknad.startSoknad()
                .medAktorId(AKTORID)
                .medskjemaNummer(SKJEMANUMMER)
                .medJournalforendeEnhet(JOURNALFORENDEENHET)
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()));
        ArgumentCaptor<WebSoknad> soknadCaptor = ArgumentCaptor.forClass(WebSoknad.class);
        when(lokalDb.hentNyesteSoknadGittBehandlingskjedeId(eq(BEHANDLINGSID))).thenReturn(soknad);
        when(lokalDb.opprettSoknad(soknadCaptor.capture())).thenReturn(SOKNADID);

        String nyBehandlingsId = ettersendingService.start(BEHANDLINGSID, AKTORID);

        verify(lokalDb, times(1)).opprettSoknad(any());
        WebSoknad lagretSoknad = soknadCaptor.getValue();
        assertEquals(nyBehandlingsId, lagretSoknad.getBrukerBehandlingId());
        assertEquals(BEHANDLINGSID, lagretSoknad.getBehandlingskjedeId());
        assertEquals(AKTORID, lagretSoknad.getAktoerId());
        assertEquals(SKJEMANUMMER, lagretSoknad.getskjemaNummer());
        assertEquals(JOURNALFORENDEENHET, lagretSoknad.getJournalforendeEnhet());
        assertEquals(emptyList(), lagretSoknad.getVedlegg());
        assertEquals(SoknadInnsendingStatus.UNDER_ARBEID, lagretSoknad.getStatus());
        assertEquals(DelstegStatus.ETTERSENDING_OPPRETTET, lagretSoknad.getDelstegStatus());
    }

    @Test
    public void finnerSoknadIDb_EttersendingOpprettesMedDetVedleggSomIkkeErKvittering() {
        String persistedVedleggName = "PersistedVedlegg";
        Vedlegg v0 = createVedlegg().medSkjemaNummer("v0").medNavn(persistedVedleggName);
        Vedlegg v1 = createVedlegg().medSkjemaNummer(SKJEMANUMMER).medNavn("Vedlegg1");
        Vedlegg v2 = createVedlegg().medSkjemaNummer(SKJEMANUMMER_KVITTERING).medNavn("Kvittering");
        List<Vedlegg> vedlegg = asList(v0, v1, v2);
        Vedlegg expectedVedlegg = expectedVedlegg(v0);

        WebSoknad soknad = WebSoknad.startSoknad()
                .medAktorId(AKTORID)
                .medskjemaNummer(SKJEMANUMMER)
                .medInnsendtDato(Timestamp.valueOf(LocalDateTime.now()))
                .medVedlegg(vedlegg);
        ArgumentCaptor<WebSoknad> soknadCaptor = ArgumentCaptor.forClass(WebSoknad.class);
        when(lokalDb.hentNyesteSoknadGittBehandlingskjedeId(eq(BEHANDLINGSID))).thenReturn(soknad);
        when(lokalDb.opprettSoknad(soknadCaptor.capture())).thenReturn(SOKNADID);

        String nyBehandlingsId = ettersendingService.start(BEHANDLINGSID, AKTORID);

        verify(lokalDb, times(1)).opprettSoknad(any());
        verify(vedleggHentOgPersistService, times(1)).persisterVedlegg(eq(nyBehandlingsId), eq(singletonList(expectedVedlegg)));
        WebSoknad lagretSoknad = soknadCaptor.getValue();
        assertEquals(1, lagretSoknad.getVedlegg().size());
        assertEquals(persistedVedleggName, lagretSoknad.getVedlegg().get(0).getNavn());
    }


    private Vedlegg expectedVedlegg(Vedlegg v) {
        return new Vedlegg()
                .medSoknadId(SOKNADID)
                .medNavn(v.getNavn())
                .medSkjemaNummer(v.getSkjemaNummer())
                .medInnsendingsvalg(v.getInnsendingsvalg())
                .medOpprinneligInnsendingsvalg(v.getInnsendingsvalg())
                .medVedleggId(null)
                .medAntallSider(0)
                .medStorrelse(0L)
                .medFillagerReferanse(null);
    }

    private Vedlegg createVedlegg() {
        return new Vedlegg()
                .medSoknadId(38L)
                .medNavn(UUID.randomUUID().toString())
                .medSkjemaNummer(UUID.randomUUID().toString())
                .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                .medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp)
                .medVedleggId(65L)
                .medAntallSider(8)
                .medStorrelse(15L);
    }

    private Faktum expectedFaktum(WebSoknad soknad) {
        return new Faktum()
                .medSoknadId(SOKNADID)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(soknad.getInnsendtDato().getMillis()))
                .medType(SYSTEMREGISTRERT);
    }
}
