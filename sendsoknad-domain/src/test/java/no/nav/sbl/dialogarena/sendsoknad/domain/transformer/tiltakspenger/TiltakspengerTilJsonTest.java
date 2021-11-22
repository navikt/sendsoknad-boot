package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TiltakspengerTilJsonTest {

    @Test
    public void skalKonvertereTilAlternativRepresentasjon() {
        // given
        Faktum faktum1 = new Faktum().medKey("foo.bar").medProperty("kvp", "false");
        WebSoknad webSoknad = new WebSoknad().medFaktum(faktum1);

        //when
        AlternativRepresentasjon result = new TiltakspengerTilJson().apply(webSoknad);

        // then
        assertEquals("application/json", result.getMimetype());
        assertEquals("Tiltakspenger.json", result.getFilnavn());
        assertNotNull(result.getContent());
    }
}
