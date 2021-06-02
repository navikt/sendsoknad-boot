package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ForventningsSjekkerTest {

    @Test
    public void skalKunneSammenligneDoublesOgInt() {
        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6")));
        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6,0")));
        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6", new Faktum().medValue("6.0")));

        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6")));
        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6.0")));
        assertTrue(ForventningsSjekker.sjekkForventning("value >= 6.0", new Faktum().medValue("6,0")));
        assertTrue(ForventningsSjekker.sjekkForventning("6.0 <= value", new Faktum().medValue("6,0")));
    }

    @Test
    public void skalKunneSammenligneStrings() {
        assertTrue(ForventningsSjekker.sjekkForventning("value == 'en'", new Faktum().medValue("en")));
        assertFalse(ForventningsSjekker.sjekkForventning("value == 'en'", new Faktum().medValue("to")));
    }

    @Test
    public void skalKunneSammenligneNull() {
        assertTrue(ForventningsSjekker.sjekkForventning("value != null && value != 'en'", new Faktum().medValue("to")));
        assertTrue(ForventningsSjekker.sjekkForventning("value != null && value != 'en'", new Faktum().medValue("")));
        assertFalse(ForventningsSjekker.sjekkForventning("value != null && value != 'en'", new Faktum().medValue(null)));
        assertFalse(ForventningsSjekker.sjekkForventning("value != null && value != ''", new Faktum().medValue("")));
    }
}
