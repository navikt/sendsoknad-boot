package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TilgangskontrollTest {

    @InjectMocks
    private Tilgangskontroll tilgangskontroll;
    @Mock
    private SoknadService soknadService;


    @BeforeClass
    public static void initializeTokenValidationContext() throws Exception {
       TestTokenUtils.setSecurityContext();
    }

    @Test
    public void skalGiTilgangForBruker() {
        String aktorId = "10108000398"; // Ikke ekte person
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId(aktorId));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("123");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileForAndre() {
        when(soknadService.hentSoknad("XXX", false, false)).thenReturn(new WebSoknad().medAktorId("other_user"));
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad("XXX");
    }

    @Test(expected = AuthorizationException.class)
    public void skalFeileHvisEierErNull() {
        tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(null, null);
    }
}
