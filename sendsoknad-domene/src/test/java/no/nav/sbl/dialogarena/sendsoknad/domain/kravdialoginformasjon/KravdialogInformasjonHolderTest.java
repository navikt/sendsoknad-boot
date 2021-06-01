package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class KravdialogInformasjonHolderTest {

    @Test
    public void skalHenteKonfigBasertPaaSkjemanummer() {
        KravdialogInformasjon konfigurasjon = KravdialogInformasjonHolder.hentKonfigurasjon("NAV 11-13.05");
        assertTrue(konfigurasjon.getSkjemanummer().contains("NAV 11-13.05"));
    }

    @Test(expected = SendSoknadException.class)
    public void skalKasteFeilHvisSkjemanummerIkkeFinnes() {
        KravdialogInformasjonHolder.hentKonfigurasjon("skjemaSomIkkeFinnes");
    }
}
