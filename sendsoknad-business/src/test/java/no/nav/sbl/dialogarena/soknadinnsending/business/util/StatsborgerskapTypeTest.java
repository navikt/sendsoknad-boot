package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatsborgerskapTypeTest {

    @Test
    public void skalReturnereNorskForLandkodeNOR() {
        assertEquals("norsk", StatsborgerskapType.get("NOR"));
    }

    @Test
    public void skalReturnereEOSForLandkodeDNK() {
        assertEquals("eos", StatsborgerskapType.get("DNK"));
    }

    @Test
    public void skalReturnereIkkeEosForLandkodeBUR() {
        assertEquals("ikkeEos", StatsborgerskapType.get("BUR"));
    }
}
