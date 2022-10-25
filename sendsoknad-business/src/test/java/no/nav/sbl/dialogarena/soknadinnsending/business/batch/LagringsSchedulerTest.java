package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler;
    @Mock private SoknadRepository soknadRepository;
    @Mock private FillagerService fillagerService;
    @Mock private HenvendelseService henvendelseService;


    @Test
    public void skalFlytteAlleSoknaderTilHenvendelse() throws InterruptedException {
        System.setProperty("sendsoknad.batch.enabled", "true");
        Optional<WebSoknad> tom = Optional.empty();
        Optional<WebSoknad> soknad = Optional.of(new WebSoknad().medId(1).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(soknad).thenReturn(soknad).thenReturn(tom);
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(soknadRepository, times(2)).slettSoknad(eq(soknad.get()), eq(HendelseType.LAGRET_I_HENVENDELSE));
    }

    @Test
    public void skalLagreSoknadIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad().medId(1).medAktorId("11111111111").medBehandlingId("1").medUuid("1234").medStatus(SoknadInnsendingStatus.UNDER_ARBEID);
        scheduler.lagreFilTilHenvendelseOgSlettILokalDb(webSoknad);
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            verify(fillagerService).lagreFil(eq(webSoknad.getBrukerBehandlingId()), eq(webSoknad.getUuid()), eq(webSoknad.getAktoerId()), any(InputStream.class));
        }
        verify(soknadRepository).slettSoknad(eq(webSoknad), eq(HendelseType.LAGRET_I_HENVENDELSE));
    }

    @Test
    public void skalAvbryteIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        String behandlingsId = "1";
        int soknadId = 1;
        WebSoknad webSoknad = new WebSoknad()
                .medId(soknadId)
                .medAktorId("11111111111")
                .medBehandlingId(behandlingsId)
                .medUuid("1234")
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID);
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(Optional.of(webSoknad)).thenReturn(Optional.empty());
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(henvendelseService).avbrytSoknad(behandlingsId);
        verify(soknadRepository).slettSoknad(eq(webSoknad), eq(HendelseType.LAGRET_I_HENVENDELSE));
    }

    @Test
    public void leggerTilbakeSoknadenHvisNoeFeiler() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad();
        when(soknadRepository.plukkSoknadTilMellomlagring())
                .thenReturn(Optional.of(webSoknad))
                .thenReturn(Optional.empty());
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(soknadRepository).leggTilbake(webSoknad);
    }
}
