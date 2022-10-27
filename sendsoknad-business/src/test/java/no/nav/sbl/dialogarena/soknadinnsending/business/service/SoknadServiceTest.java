package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private Brukernotifikasjon brukernotifikasjon;

    @InjectMocks
    private SoknadService soknadService;


    @Before
    public void setup() {
        ReflectionTestUtils.setField(soknadService, "sendDirectlyToSoknadsmottaker", true);
    }

    @Test
    public void skalSetteDelsteg() {
        soknadService.settDelsteg("1", OPPRETTET);
        verify(soknadRepository).settDelstegstatus("1", OPPRETTET);
    }

    @Test
    public void skalSetteJournalforendeEnhet() {
        soknadService.settJournalforendeEnhet("1", "1234");
        verify(soknadRepository).settJournalforendeEnhet("1", "1234");
    }

    @Test
    public void skalHenteSoknad() {
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medskjemaNummer("NAV 11-12.12"));

        WebSoknad soknad = soknadService.hentSoknadFraLokalDb(1L);

        assertThat(soknad).isEqualTo(new WebSoknad().medId(1L).medskjemaNummer("NAV 11-12.12"));
    }

    @Test
    public void skalAvbryteSoknad() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medId(11L);
        when(soknadRepository.hentSoknad("123")).thenReturn(soknad);

        soknadService.avbrytSoknad("123");

        verify(soknadRepository).slettSoknad(soknad, HendelseType.AVBRUTT_AV_BRUKER);
        verify(henvendelsesConnector).avbrytSoknad("123");
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) verify(fillagerService).slettAlle("123");
        verify(soknadMetricsService).avbruttSoknad(eq(null), eq(false));
        verify(brukernotifikasjon, times(1)).cancelNotification(eq("123"), any(), eq(false), any());
    }

    @Test
    public void skalHenteSoknadsIdForEttersendingTilBehandlingskjedeId() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.of(soknad));

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad.getSoknadId()).isEqualTo(1L);
    }

    @Test
    public void skalFaNullNarManProverAHenteEttersendingMedBehandlingskjedeIdSomIkkeHarNoenEttersending() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.empty());

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad).isNull();
    }
}
