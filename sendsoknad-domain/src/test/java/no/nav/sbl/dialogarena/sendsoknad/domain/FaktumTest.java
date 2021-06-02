package no.nav.sbl.dialogarena.sendsoknad.domain;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FaktumTest {
    private Faktum faktum;

    @Before
    public void setup() {
        faktum = lagFaktum();
    }

    private Faktum lagFaktum() {
        return new Faktum()
                .medKey("nokkel")
                .medType(Faktum.FaktumType.BRUKERREGISTRERT)
                .medProperty("key1", "value1")
                .medProperty("key2", "value2")
                .medSystemProperty("system1", "value2")
                .medSystemProperty("system2", "value2");
    }

    @Test
    public void skalKorrektHenteTypeString(){
        assertEquals(Faktum.FaktumType.BRUKERREGISTRERT.name(), lagFaktum().getTypeString());
    }
    @Test
    public void skalHaEgenskap() {
        assertTrue(faktum.hasEgenskap("key1"));
        assertTrue(faktum.hasEgenskap("key2"));
        assertFalse(faktum.hasEgenskap("ikkeekstisterende"));
    }

    @Test
    public void skalKopiereSystemlagrede(){
        Faktum sysFaktum = lagFaktum();
        sysFaktum.finnEgenskap("system1").setValue("ikkeEndret");
        sysFaktum.finnEgenskap("system2").setValue("ikkeEndret");
        faktum.medSystemProperty("system3", "value3");
        faktum.kopierSystemlagrede(sysFaktum);
        assertNull(faktum.finnEgenskap("system3"));
        assertEquals("ikkeEndret", faktum.finnEgenskap("system1").getValue());
        assertEquals("ikkeEndret", faktum.finnEgenskap("system2").getValue());
        assertEquals("value1", faktum.finnEgenskap("key1").getValue());
        assertEquals("value2", faktum.finnEgenskap("key2").getValue());
    }
    @Test
    public void skalKopiereBrukerlagrede(){
        Faktum faktum = new Faktum().medSystemProperty("system1", "sysVerdi1");
        faktum.kopierFaktumegenskaper(lagFaktum());
        assertEquals("sysVerdi1", faktum.finnEgenskap("system1").getValue());
        assertNull(faktum.finnEgenskap("system2"));
        assertEquals("value1", faktum.finnEgenskap("key1").getValue());
        assertEquals("value2", faktum.finnEgenskap("key2").getValue());
    }
}
