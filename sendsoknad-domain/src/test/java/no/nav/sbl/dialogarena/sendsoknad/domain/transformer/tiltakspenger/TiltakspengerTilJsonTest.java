package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.jupiter.api.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger.TiltakspengerTilJson.FILNAVN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class TiltakspengerTilJsonTest {

    @Test
    public void skalKonvertereTilAlternativRepresentasjon() {
        // given
        WebSoknad webSoknad = new WebSoknad();

        //when
        AlternativRepresentasjon result = new TiltakspengerTilJson().apply(webSoknad);

        // then
        assertEquals(APPLICATION_JSON_VALUE, result.getMimetype());
        assertEquals(FILNAVN, result.getFilnavn());
        assertNotNull(result.getContent());
    }
}
