package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class BilstonadInformasjonTest {

    private BilstonadInformasjon bilstonadInformasjon = new BilstonadInformasjon();

    @Test
    public void verifiserSkjemanummer() {
        List<String> expected = Arrays.asList("NAV 10-07.40", "NAV 10-07.41");

        List<String> actual = bilstonadInformasjon.getSkjemanummer();

        assertTrue(actual.containsAll(expected));
    }
}
