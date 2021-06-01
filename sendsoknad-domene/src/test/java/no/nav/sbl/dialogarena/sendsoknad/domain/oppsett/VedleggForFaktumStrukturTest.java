package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VedleggForFaktumStrukturTest {

    private Faktum konkurs = new Faktum().medKey("arbeidforhold").medProperty("type", "arbeidsgivererkonkurs");
    private Faktum permittert = new Faktum().medKey("arbeidforhold").medProperty("type", "permittert");
    private Faktum sagtOppAvArbeidsgiver = new Faktum().medKey("arbeidforhold").medProperty("type", "sagtoppavarbeidsgiver");

    @Test
    public void skalFikseFlereOnValues() {
        VedleggForFaktumStruktur vedlegg = new VedleggForFaktumStruktur();
        vedlegg.setOnValues(Arrays.asList("arbeidsgivererkonkurs", "permittert"));
        vedlegg.setOnProperty("type");
        vedlegg.setInverted(true);

        assertFalse(vedlegg.trengerVedlegg(konkurs));
        assertFalse(vedlegg.trengerVedlegg(permittert));
        assertTrue(vedlegg.trengerVedlegg(sagtOppAvArbeidsgiver));

        vedlegg.setInverted(false);
        assertTrue(vedlegg.trengerVedlegg(konkurs));
        assertTrue(vedlegg.trengerVedlegg(permittert));
        assertFalse(vedlegg.trengerVedlegg(sagtOppAvArbeidsgiver));
    }
}
