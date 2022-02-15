package no.nav.sbl.dialogarena.rest.ressurser;

import static no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs.XSRF_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.common.auth.SubjectHandler;
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
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, null);
    }

    @Test
    public void oppdaterSoknadMedDelstegUtfyllingSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "utfylling", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.UTFYLLING);
    }

    @Test
    public void oppdaterSoknadMedDelstegOpprettetSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "opprettet", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.OPPRETTET);
    }

    @Test
    public void oppdaterSoknadMedDelstegVedleggSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "vedlegg", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedDelstegOppsummeringSkalSetteRiktigDelstegStatus() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, "oppsummering", null);
        verify(soknadService).settDelsteg(BEHANDLINGSID, DelstegStatus.VEDLEGG_VALIDERT);
    }

    @Test
    public void oppdaterSoknadMedJournalforendeenhetSkalSetteJournalforendeEnhet() {
        ressurs.oppdaterSoknad(BEHANDLINGSID, null, "NAV UTLAND");
        verify(soknadService).settJournalforendeEnhet(BEHANDLINGSID, "NAV UTLAND");
    }
}
