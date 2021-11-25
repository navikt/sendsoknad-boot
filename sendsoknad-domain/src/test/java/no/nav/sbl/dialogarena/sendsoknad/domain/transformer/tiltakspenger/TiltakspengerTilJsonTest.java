package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import junit.framework.AssertionFailedError;
import net.minidev.json.JSONObject;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import org.junit.jupiter.api.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TiltakspengerTilJsonTest {

    private JsonTiltakspengerSoknad jsonSoknad;

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
    public void skalKonverterefaktumStruktur() {
        WebSoknad soknad = new WebSoknad()
                .medId(1234)
                .medskjemaNummer("NAV 76-13.45")
                .medStatus(UNDER_ARBEID)
                .medAktorId("12345")
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medFaktum(new Faktum().medKey("informasjonsside.deltarIIntroprogram").medValue("false"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon").medValue("true"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon.ja.hvaslags").medValue("fengsel"))
                .medFaktum(new Faktum().medKey("informasjonsside.kvalifiseringsprogram").medValue("false"));


        jsonSoknad = new TiltakspengerTilJson().transform(soknad);

        assertThat(jsonSoknad.getSkjemaNummer()).isEqualTo("NAV 76-13.45");
        assertThat(jsonSoknad.getAktoerId()).isEqualTo("12345");
        assertThat(jsonSoknad.getStatus()).isEqualTo(UNDER_ARBEID);
        assertThat(jsonSoknad.getFakta()).hasSize(4);
        assertThat(getFaktum("informasjonsside.deltarIIntroprogram").getValue()).isEqualTo("false");
        assertThat(getFaktum("informasjonsside.institusjon").getValue()).isEqualTo("true");
        assertThat(getFaktum("informasjonsside.institusjon.ja.hvaslags").getValue()).isEqualTo("fengsel");
        assertThat(getFaktum("informasjonsside.kvalifiseringsprogram").getValue()).isEqualTo("false");
    }

    private JsonTiltakspengerFaktum getFaktum(String key) {
        return jsonSoknad
                .getFakta()
                .stream()
                .filter(jsonTiltakspengerFaktum -> jsonTiltakspengerFaktum.getKey().equals(key))
                .findFirst()
                .orElseThrow(AssertionFailedError::new);
    }
}
