package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import junit.framework.AssertionFailedError;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.TiltakspengerInformasjon;
import org.junit.jupiter.api.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static org.junit.jupiter.api.Assertions.*;

class TiltakspengerTilJsonTest {
    private final String skjemaNummer;
    private final String filNavn;
    private JsonTiltakspengerSoknad jsonSoknad;

    {
        final TiltakspengerInformasjon tiltakspengerInformasjon = new TiltakspengerInformasjon();
        skjemaNummer = tiltakspengerInformasjon.getSkjemanummer().get(0);
        filNavn = tiltakspengerInformasjon.getStrukturFilnavn();
    }

    @Test
    public void skalKonvertereTilAlternativRepresentasjon() {
        // given
        Faktum faktum1 = new Faktum().medKey("foo.bar").medProperty("kvp", "false");
        WebSoknad webSoknad = new WebSoknad().medFaktum(faktum1);

        //when
        AlternativRepresentasjon result = new TiltakspengerTilJson().apply(webSoknad);

        // then
        assertEquals("application/json", result.getMimetype());
        assertEquals(filNavn, result.getFilnavn());
        assertNotNull(result.getContent());
    }

    @Test
    public void skalKonverterefaktumStruktur() {
        WebSoknad soknad = new WebSoknad()
                .medId(1234)
                .medskjemaNummer(skjemaNummer)
                .medStatus(UNDER_ARBEID)
                .medAktorId("12345")
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medFaktum(new Faktum().medKey("informasjonsside.deltarIIntroprogram").medValue("false"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon").medValue("true"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon.ja.hvaslags").medValue("fengsel"))
                .medFaktum(new Faktum().medKey("informasjonsside.kvalifiseringsprogram").medValue("false"));

        jsonSoknad = JsonTiltakspengerSoknadConverter.tilJsonSoknad(soknad);

        assertEquals(skjemaNummer, jsonSoknad.getSkjemaNummer());
        assertEquals("12345", jsonSoknad.getAktoerId());
        assertEquals(UNDER_ARBEID, jsonSoknad.getStatus());
        assertEquals(4, jsonSoknad.getFakta().size());
        assertFalse(Boolean.parseBoolean(getFaktum("informasjonsside.deltarIIntroprogram").getValue()));
        assertTrue(Boolean.parseBoolean(getFaktum("informasjonsside.institusjon").getValue()));
        assertEquals("fengsel", getFaktum("informasjonsside.institusjon.ja.hvaslags").getValue());
        assertFalse(Boolean.parseBoolean(getFaktum("informasjonsside.kvalifiseringsprogram").getValue()));
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
