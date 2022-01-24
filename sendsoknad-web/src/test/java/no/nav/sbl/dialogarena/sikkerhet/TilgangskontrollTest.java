package no.nav.sbl.dialogarena.sikkerhet;

import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.utils.TestTokenUtils;

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
      
      
        when(soknadService.hentSoknad("123", false, false)).thenReturn(new WebSoknad().medAktorId("***REMOVED***"));
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
