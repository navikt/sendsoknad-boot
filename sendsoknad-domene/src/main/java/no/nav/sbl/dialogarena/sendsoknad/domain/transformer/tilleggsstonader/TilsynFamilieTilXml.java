package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterFamilie;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import static java.util.stream.Collectors.joining;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.GET_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.propertyIsValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.TilsynForetasAv.TO_TILSYN_FORETAS_AV_ENUM;

public class TilsynFamilieTilXml {

    public static TilsynsutgifterFamilie transform(WebSoknad soknad) {
        TilsynsutgifterFamilie familie = new TilsynsutgifterFamilie();
        familie.setPeriode(extractValue(soknad.getFaktumMedKey("tilsynfamilie.periode"), Periode.class));
        familie.setTilsynsmottaker(extractValue(soknad.getFaktumMedKey("tilsynfamilie.persontilsyn"), String.class));
        familie.setTilsynForetasAv(foretasAv(soknad.getFaktumMedKey("tilsynfamilie.typetilsyn")));
        Boolean deletilsyn = extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), Boolean.class);
        familie.setDeltTilsyn(deletilsyn);
        if (deletilsyn != null && deletilsyn) {
            familie.setAnnenTilsynsperson(extractValue(soknad.getFaktumMedKey("tilsynfamilie.deletilsyn"), String.class, "personnummer"));
        }

        return familie;
    }

    private static String foretasAv(final Faktum faktum) {
        return faktum.getProperties().entrySet().stream()
                .filter(propertyIsValue("true"))
                .map(GET_KEY)
                .map(TO_TILSYN_FORETAS_AV_ENUM)
                .collect(joining(","));
    }
}
