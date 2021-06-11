package no.nav.sbl.dialogarena.soknadinnsending.business.db;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IdGeneratorTest {

    @Test
    public void skalGenererId() {
        String behandlingsId = IdGenerator.lagBehandlingsId(1L);
        assertEquals("100000001", behandlingsId);
    }

    @Test(expected = RuntimeException.class)
    public void skalFaaFeilVedForHoyId() {
        IdGenerator.lagBehandlingsId(10000000000000L);
        fail("Expected exception");
    }
}
