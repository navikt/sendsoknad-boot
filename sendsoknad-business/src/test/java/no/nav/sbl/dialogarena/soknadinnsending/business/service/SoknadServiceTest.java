package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.Brukernotifikasjon;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static java.util.Collections.singletonList;
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
    private SoknadMetricsService soknadMetricsService;
    @Mock
    private Brukernotifikasjon brukernotifikasjon;
    @Mock
    private SoknadDataFletter soknadDataFletter;

    @InjectMocks
    private SoknadService soknadService;


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
        String behandlingsId = "123";
        Vedlegg vedlegg = new Vedlegg().medStorrelse(71L).medInnsendingsvalg(Vedlegg.Status.LastetOpp);
        WebSoknad soknad = new WebSoknad()
                .medBehandlingId(behandlingsId)
                .medId(11L)
                .medVedlegg(vedlegg);
        when(soknadRepository.hentSoknad(behandlingsId)).thenReturn(soknad);

        soknadService.avbrytSoknad(behandlingsId);

        verify(soknadDataFletter, times(1)).deleteFiles(eq(behandlingsId), eq(singletonList(vedlegg.getFillagerReferanse())));
        verify(soknadRepository).slettSoknadPermanent(soknad.getSoknadId(), HendelseType.PERMANENT_SLETTET_AV_BRUKER);
        verify(soknadMetricsService).avbruttSoknad(eq(null), eq(false));
        verify(brukernotifikasjon, times(1)).cancelNotification(eq(behandlingsId), any(), eq(false), any());
    }

    @Test
    public void skalAutomatiskAvbryteSoknad() {
        HendelseType hendelseType = HendelseType.AVBRUTT_AUTOMATISK;
        int dagerGamle = 7*8;

        WebSoknad gammelIkkeInnsendt = lagSoknad(1L, SoknadInnsendingStatus.UNDER_ARBEID,  DateTime.now().minusDays( 7*8 +1));

        List<WebSoknad> soknader = new LinkedList<>();
        soknader.add(gammelIkkeInnsendt);
        when(soknadRepository.slettGamleIkkeInnsendteSoknader(dagerGamle)).thenReturn(soknader);

        soknadService.automatiskSlettingAvSoknader(hendelseType, false, dagerGamle);

        verify(soknadDataFletter, times(1))
                .deleteFiles(eq(gammelIkkeInnsendt.getBrukerBehandlingId()), eq(singletonList(gammelIkkeInnsendt.getVedlegg().stream().findFirst().get().getFillagerReferanse())));
        verify(soknadRepository).slettGamleIkkeInnsendteSoknader( dagerGamle);
        //verify(soknadMetricsService).avbruttSoknad(eq(null), eq(false));
        verify(brukernotifikasjon, times(1)).cancelNotification(eq(gammelIkkeInnsendt.getBrukerBehandlingId()), any(), eq(false), any());
    }

    @Test
    public void skalAutomatiskSlettePermanentSoknad() {
        HendelseType hendelseType = HendelseType.PERMANENT_SLETTET_AV_SYSTEM;
        int dagerGamle = 7*8;

        WebSoknad gammelAvbrutt = lagSoknad(1L, SoknadInnsendingStatus.AVBRUTT_AUTOMATISK,  DateTime.now().minusDays( 7*26 +1));
        WebSoknad gammelFerdig = lagSoknad(1L, SoknadInnsendingStatus.FERDIG,  DateTime.now().minusDays( 7*26 +1));

        List<WebSoknad> soknader = new LinkedList<>();
        soknader.add(gammelAvbrutt);
        soknader.add(gammelFerdig);
        when(soknadRepository.slettGamleSoknaderPermanent(dagerGamle)).thenReturn(soknader);

        soknadService.automatiskSlettingAvSoknader(hendelseType, false, dagerGamle);

        verify(soknadDataFletter, times(2)).deleteFiles(any(), any());
        verify(soknadRepository).slettGamleSoknaderPermanent(dagerGamle);
        //verify(soknadMetricsService).avbruttSoknad(eq(null), eq(false));
        verify(brukernotifikasjon, times(0)).cancelNotification(any(), any(), eq(false), any());
    }

    private WebSoknad lagSoknad(Long id, SoknadInnsendingStatus status, DateTime opprettetDato) {
        Vedlegg vedlegg = new Vedlegg().medStorrelse(71L).medInnsendingsvalg(Vedlegg.Status.LastetOpp);
        return new WebSoknad()
                .medBehandlingId(UUID.randomUUID().toString())
                .medId(id)
                .medStatus(status)
                .medOppretteDato(DateTime.now().minusDays( 7*8 +1))
                .medArkivStatus(SoknadArkiveringsStatus.IkkeSatt)
                .medVedlegg(vedlegg);
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