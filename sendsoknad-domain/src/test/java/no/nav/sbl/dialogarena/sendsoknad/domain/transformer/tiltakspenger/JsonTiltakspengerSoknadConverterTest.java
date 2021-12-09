package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import junit.framework.AssertionFailedError;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.jupiter.api.Test;

import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.TiltakspengerInformasjon.SKJEMANUMMER;
import static org.junit.jupiter.api.Assertions.*;

class JsonTiltakspengerSoknadConverterTest {
    private JsonTiltakspengerSoknad jsonSoknad;

    @Test
    public void skalKonverterefaktumStruktur() {
        WebSoknad soknad = new WebSoknad()
                .medId(1234)
                .medskjemaNummer(SKJEMANUMMER)
                .medStatus(UNDER_ARBEID)
                .medAktorId("12345")
                .medDelstegStatus(DelstegStatus.VEDLEGG_VALIDERT)
                .medFaktum(new Faktum().medKey("informasjonsside.deltarIIntroprogram").medValue("false"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon").medValue("true"))
                .medFaktum(new Faktum().medKey("informasjonsside.institusjon.ja.hvaslags").medValue("fengsel"))
                .medFaktum(new Faktum().medKey("informasjonsside.kvalifiseringsprogram").medValue("false"))
                .medVedlegg(new Vedlegg()
                        .medSkjemaNummer("02")
                        .medSkjemanummerTillegg("kontraktutgaatt")
                        .medInnsendingsvalg(LastetOpp)
                        .medNavn("Navn på vedlegg")
                        .medStorrelse(12345L)
                        .medAntallSider(1));

        jsonSoknad = JsonTiltakspengerSoknadConverter.tilJsonSoknad(soknad);

        assertEquals(SKJEMANUMMER, jsonSoknad.getSkjemaNummer());
        assertEquals("12345", jsonSoknad.getAktoerId());
        assertEquals(UNDER_ARBEID, jsonSoknad.getStatus());
        assertEquals(4, jsonSoknad.getFakta().size());
        assertFalse(Boolean.parseBoolean(getFaktum("informasjonsside.deltarIIntroprogram").getValue()));
        assertTrue(Boolean.parseBoolean(getFaktum("informasjonsside.institusjon").getValue()));
        assertEquals("fengsel", getFaktum("informasjonsside.institusjon.ja.hvaslags").getValue());
        assertFalse(Boolean.parseBoolean(getFaktum("informasjonsside.kvalifiseringsprogram").getValue()));
        assertEquals("02", jsonSoknad.getVedlegg().get(0).getSkjemaNummer());
        assertEquals("kontraktutgaatt", jsonSoknad.getVedlegg().get(0).getSkjemanummerTillegg());
        assertEquals(LastetOpp, jsonSoknad.getVedlegg().get(0).getInnsendingsvalg());
        assertEquals("Navn på vedlegg", jsonSoknad.getVedlegg().get(0).getNavn());
        assertEquals(12345L, jsonSoknad.getVedlegg().get(0).getStorrelse());
        assertEquals(1, jsonSoknad.getVedlegg().get(0).getAntallSider());
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