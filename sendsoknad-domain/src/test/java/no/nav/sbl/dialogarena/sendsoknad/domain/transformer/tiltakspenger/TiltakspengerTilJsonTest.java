package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void konvertererFaktumRett() {
        Faktum faktum1 = new Faktum().medKey("informasjonsside.deltarIIntroprogram").medValue("false");
        Faktum faktum2 = new Faktum().medKey("informasjonsside.institusjon").medValue("true");
        Faktum faktum3 = new Faktum().medKey("informasjonsside.institusjon.ja.hvaslags").medValue("fengsel");
        Faktum faktum4 = new Faktum().medKey("informasjonsside.kvalifiseringsprogram").medValue("false");
        WebSoknad webSoknad = new WebSoknad().medFaktum(faktum1).medFaktum(faktum2).medFaktum(faktum3).medFaktum(faktum4);

        AlternativRepresentasjon result = new TiltakspengerTilJson().apply(webSoknad);

        // TODO: konvertere til json fra content
        //String json = result.getContent();
        //TODO: hent verdien fra informasjonsside.institusjon
        //assertTrue(json.);
        
    }
}
