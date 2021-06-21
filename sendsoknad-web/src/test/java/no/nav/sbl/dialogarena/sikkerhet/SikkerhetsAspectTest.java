package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.ws.rs.NotFoundException;
import java.lang.annotation.Annotation;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.*;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SikkerhetsAspectTest {

    @Mock
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private VedleggService vedleggService;
    @Mock
    private FaktaService faktaService;
    @InjectMocks
    private SikkerhetsAspect sikkerhetsAspect;

    private String brukerBehandlingsId = "1";

    @Before
    public void init() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
    }

    @Test
    public void skalSjekkeSikkerhetForBehandling() {
        setup(generateXsrfToken(brukerBehandlingsId));
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(brukerBehandlingsId, getSjekkTilgangTilSoknad(Behandling));
        verifyNoMoreInteractions(faktaService, vedleggService);
    }

    @Test
    public void skalSjekkeOmBrukerHarTilgangTilFakta() {
        setup(generateXsrfToken(brukerBehandlingsId));
        when(faktaService.hentBehandlingsId(1L)).thenReturn(brukerBehandlingsId);
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(1L, getSjekkTilgangTilSoknad(Faktum));
        verify(faktaService, times(1)).hentBehandlingsId(1L);
    }

    @Test(expected = NotFoundException.class)
    public void skalGiNotFoundExceptionOmRessursIkkeFinnes() {
        setup(generateXsrfToken(brukerBehandlingsId));
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(1L, getSjekkTilgangTilSoknad(Faktum));
    }

    @Test
    public void skalSjekkeOmBrukerHarTilgangTilVedlegg() {
        setup(generateXsrfToken(brukerBehandlingsId));
        when(vedleggService.hentBehandlingsId(1L)).thenReturn(brukerBehandlingsId);
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(1L, getSjekkTilgangTilSoknad(Vedlegg));
        verify(vedleggService, times(1)).hentBehandlingsId(1L);
    }

    @Test(expected = NotFoundException.class)
    public void skalHandtereHvisIkkeVedleggFinnes() {
        setup(generateXsrfToken(brukerBehandlingsId));
        when(vedleggService.hentBehandlingsId(1L)).thenReturn(null);
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(1L, getSjekkTilgangTilSoknad(Vedlegg));
    }

    @Test(expected = AuthorizationException.class)
    public void skalKasteExceptionNaarTokenIkkeStemmer() {
        setup("tulletoken");
        sikkerhetsAspect.sjekkOmBrukerHarTilgang(brukerBehandlingsId, getSjekkTilgangTilSoknad(Behandling));
    }

    @Test
    public void soknadRessursSkalBliMatchetAvAspectet() {
        setup("token");
        AspectJProxyFactory f = new AspectJProxyFactory(new Testclass());
        SikkerhetsAspect sa = spy(new SikkerhetsAspectTestExtension());
        f.addAspect(sa);
        Testinterface proxy = f.getProxy();
        doNothing().when(sa).sjekkOmBrukerHarTilgang(any(), any(SjekkTilgangTilSoknad.class));

        proxy.behandlingMetode("test");
        proxy.vedleggMetode(2L);

        ArgumentCaptor<SjekkTilgangTilSoknad> argumentCaptor = ArgumentCaptor.forClass(SjekkTilgangTilSoknad.class);
        verify(sa).sjekkOmBrukerHarTilgang(eq("test"), argumentCaptor.capture());
        assertEquals(Behandling, argumentCaptor.getValue().type());
        verify(sa).sjekkOmBrukerHarTilgang(eq(2L), argumentCaptor.capture());
        assertEquals(Vedlegg, argumentCaptor.getValue().type());
    }

    private static class SikkerhetsAspectTestExtension extends SikkerhetsAspect { }

    interface Testinterface {
        void behandlingMetode(String behandlingsId);
        void vedleggMetode(Long vedleggId);
    }

    private static class Testclass implements Testinterface {
        @SjekkTilgangTilSoknad
        public void behandlingMetode(String behandlingsId) {
        }

        @SjekkTilgangTilSoknad(type = Vedlegg)
        public void vedleggMetode(Long vedleggId) {
        }
    }

    private static SjekkTilgangTilSoknad getSjekkTilgangTilSoknad(final SjekkTilgangTilSoknad.Type type) {
        return new SjekkTilgangTilSoknad() {
            @Override
            public boolean sjekkXsrf() {
                return true;
            }

            @Override
            public Type type() {
                return type;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SjekkTilgangTilSoknad.class;
            }
        };
    }

    private void setup(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest(null, null, null);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.setMethod("POST");
        request.addHeader("X-XSRF-TOKEN", token);
    }
}
