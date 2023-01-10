package no.nav.sbl.dialogarena.rest.ressurser;

import static no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs.XSRF_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SoknadCannotBeChangedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;

@RunWith(MockitoJUnitRunner.class)
public class SoknadRessursTest {

    public static final String BEHANDLINGSID = "123";

    @Mock
    SoknadService soknadService;
    @InjectMocks
    SoknadRessurs ressurs;

    private StartSoknad type;
    
    @BeforeClass
    public static void initializeTokenValidationContext() throws Exception {
       TestTokenUtils.setSecurityContext();
    }

    @Before
    public void setup() {
 
        type = new StartSoknad();
    }

    @Test
    public void hentingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.hentSoknadData(BEHANDLINGSID, response);
        verify(response).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN);
    }

    @Test
    public void opprettingAvSoknadSkalSetteXsrfToken() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ArgumentCaptor<Cookie> cookie = ArgumentCaptor.forClass(Cookie.class);
        ressurs.opprettSoknad(null, type, response);
        verify(response).addCookie(cookie.capture());
        assertThat(cookie.getValue().getName()).isEqualTo(XSRF_TOKEN);
    }

    @Test
    public void opprettSoknadUtenBehandlingsidSkalStarteNySoknad() {
        ressurs.opprettSoknad(null, type, mock(HttpServletResponse.class));
        verify(soknadService).startSoknad(isNull(), eq(TestTokenUtils.SOME_DEFAULT_FNR));
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomIkkeHarEttersendingSkalStarteNyEttersending() {
        when(soknadService.hentEttersendingForBehandlingskjedeId(BEHANDLINGSID)).thenReturn(null);
        ressurs.opprettSoknad(BEHANDLINGSID, type, mock(HttpServletResponse.class));
        verify(soknadService).startEttersending(eq(BEHANDLINGSID), eq(TestTokenUtils.SOME_DEFAULT_FNR));
    }

    @Test
    public void opprettSoknadMedBehandlingsidSomHarEttersendingSkalIkkeStarteNyEttersending() {
        when(soknadService.hentEttersendingForBehandlingskjedeId(BEHANDLINGSID)).thenReturn(new WebSoknad());
        ressurs.opprettSoknad(BEHANDLINGSID, type, mock(HttpServletResponse.class));
        verify(soknadService, never()).startEttersending(eq(BEHANDLINGSID), anyString());
    }

    @Test(expected = BadRequestException.class)
    public void oppdaterSoknadUtenParametreSkalKasteException() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, null);
    }

    @Test
    public void oppdaterSoknadMedDelstegUtfyllingSkalSetteRiktigDelstegStatus() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, "utfylling", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.UTFYLLING);
    }

    @Test
    public void oppdaterSoknadMedDelstegOpprettetSkalSetteRiktigDelstegStatus() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, "opprettet", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.OPPRETTET);
    }

    @Test
    public void oppdaterSoknadMedDelstegVedleggSkalSetteRiktigDelstegStatus() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, "vedlegg", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedDelstegOppsummeringSkalSetteRiktigDelstegStatus() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, "oppsummering", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.VEDLEGG_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedDelstegFeilerDersomSoknadIkkeUnderArbeid() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.FERDIG).medBehandlingId(BEHANDLINGSID));
        assertThrows(SoknadCannotBeChangedException.class, () ->ressurs.oppdaterSoknad(BEHANDLINGSID, "oppsummering", null));
    }

    @Test
    public void oppdaterSoknadMedJournalforendeenhetSkalSetteJournalforendeEnhet() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medBehandlingId(BEHANDLINGSID));
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, "NAV UTLAND");
        verify(soknadService).settJournalforendeEnhet(BEHANDLINGSID, "NAV UTLAND");
    }

    @Test
    public void oppdaterSoknadMedJournalforendeenhetSkalFeileNarIkkeUnderArbeid() {
        when(soknadService.hentSoknad(BEHANDLINGSID, false, false)).thenReturn(new WebSoknad().medStatus(SoknadInnsendingStatus.FERDIG).medBehandlingId(BEHANDLINGSID));
        assertThrows(SoknadCannotBeChangedException.class, () ->  ressurs.oppdaterSoknad(BEHANDLINGSID, null, "NAV UTLAND"));
    }

}
