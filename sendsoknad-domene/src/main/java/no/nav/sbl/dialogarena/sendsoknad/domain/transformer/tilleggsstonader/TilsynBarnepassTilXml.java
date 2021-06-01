package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Barn;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Tilsynskategorier;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.TilsynsutgifterBarn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.FaktumPredicates.harValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class TilsynBarnepassTilXml {
    public static final String BARNEPASS_ANDREFORELDER = "barnepass.andreforelder";
    public static final String BARNEPASS_TYPER = "barnepass.typer";
    public static final String BARNEPASS_FOLLFORT_FJERDE = "barnepass.fjerdeklasse";
    public static final List<String> BARNEPASS_AARSAKER = Arrays.asList("barnepass.fjerdeklasse.langvarig", "barnepass.fjerdeklasse.trengertilsyn", "barnepass.fjerdeklasse.ingen");
    public static final String BARNEPASS_AARSAK = "barnepass.fjerdeklasse.aarsak";
    private static final String PERIODE = "barnepass.periode";
    private static final String SOKERBARNEPASS = "barnepass.sokerbarnepass";

    private static final Locale LOCALE = new Locale("nb", "NO");

    public static TilsynsutgifterBarn transform(WebSoknad soknad, TekstHenter tekstHenter) {
        TilsynsutgifterBarn tilsynsutgifterBarn = new TilsynsutgifterBarn();
        tilsynsutgifterBarn.setPeriode(extractValue(soknad.getFaktumMedKey(PERIODE), Periode.class));
        barnSomDetSokesBarnepassOm(soknad, tilsynsutgifterBarn, tekstHenter);
        tilsynsutgifterBarn.setAnnenForsoergerperson(extractValue(soknad.getFaktumMedKey(BARNEPASS_ANDREFORELDER), String.class));

        return tilsynsutgifterBarn;
    }

    private static void barnSomDetSokesBarnepassOm(WebSoknad soknad, TilsynsutgifterBarn tilsynsutgifterBarn, TekstHenter tekstHenter) {
        List<Faktum> sokerBarnepassBarn = soknad.getFaktaMedKey(SOKERBARNEPASS).stream().filter(harValue("true")).collect(toList());
        for (Faktum barnepass : sokerBarnepassBarn) {
            Faktum barn = soknad.finnFaktum(Long.valueOf(barnepass.getProperties().get("tilknyttetbarn")));
            if (barn != null) {
                Barn stofoBarn = extractValue(barn, Barn.class);

                Faktum barnepassType = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_TYPER, barnepass.getFaktumId());
                stofoBarn.setTilsynskategori(extractValue(barnepassType, Tilsynskategorier.class));

                String belopstall = barnepassType.getProperties().get(barnepassType.getValue()+"utgift");
                BigInteger barnabelop = (belopstall == null) ? BigInteger.valueOf(0) : BigInteger.valueOf(Long.parseLong(belopstall));
                stofoBarn.setMaanedligUtgiftTilsynBarn(barnabelop);

                Faktum fulfortFjerde = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_FOLLFORT_FJERDE, barnepass.getFaktumId());
                stofoBarn.setHarFullfoertFjerdeSkoleaar(extractValue(fulfortFjerde, Boolean.class));

                List<String> aarsakTilBarnepasses = aarsaker(soknad, fulfortFjerde.getFaktumId(), tekstHenter);
                stofoBarn.getAarsakTilBarnepass().addAll(aarsakTilBarnepasses);
                tilsynsutgifterBarn.getBarn().add(stofoBarn);
            }
        }
    }

    private static List<String> aarsaker(WebSoknad soknad, Long parentFaktumId, TekstHenter tekstHenter) {
        List<String> result = new ArrayList<>();

        /*
         * Pga en feil i frontend har aarsaksfaktumene i listen BARNEPASS_AARSAKER blitt erstattet
         * av et enkelt faktum BARNEPASS_AARSAK. Sistnevte har presendes ved innsending av soknad.
         *
         * Samtidig ble frontenden endret fra en checkbox med de tre BARNEPASS_AARSAKER-faktumene
         * til en radiobutton kun tilknyttet det nye BARNEPASS_AARSAK-faktumet.
         * */
        Faktum nyttAarsakFaktum = soknad.getFaktumMedKeyOgParentFaktum(BARNEPASS_AARSAK, parentFaktumId);
        if(nyttAarsakFaktum != null) {
            String nyttFaktumVerdi = extractValue(nyttAarsakFaktum, String.class);
            String key = StofoKodeverkVerdier.BarnepassAarsak.valueOf(nyttFaktumVerdi).cmsKey;
            result.add(cms(key, tekstHenter));
        } else {
            for (String faktumKey : BARNEPASS_AARSAKER) {
                if (isTrue(extractValue(soknad.getFaktumMedKeyOgParentFaktum(faktumKey, parentFaktumId), Boolean.class))) {
                    String key = StofoKodeverkVerdier.BarnepassAarsak.valueOf(faktumKey.substring(faktumKey.lastIndexOf(".") + 1)).cmsKey;
                    result.add(cms(key, tekstHenter));
                }
            }
        }
        return result;
    }

    private static String cms(String key, TekstHenter tekstHenter) {
        return tekstHenter.finnTekst(key, null, LOCALE);
    }
}
