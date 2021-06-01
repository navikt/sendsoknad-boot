package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Anbud;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Flytteutgifter;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.sumDouble;
import static org.apache.commons.lang3.BooleanUtils.isTrue;

public class FlytteutgifterTilXml {

    private static final String AARSAK = "flytting.hvorforflytte";
    private static final String NYJOBB_STARTDATO = "flytting.nyjobb.startdato";
    private static final String FLYTTEDATO = "flytting.nyjobb.flyttedato";
    private static final String ADRESSE = "flytting.nyadresse";
    private static final String FLYTTEAVSTAND = "flytting.flytteselv.hvorlangt";
    private static final String HENGERLEIE = "flytting.flytteselv.andreutgifter.hengerleie";
    private static final String BOM = "flytting.flytteselv.andreutgifter.bom";
    private static final String PARKERING = "flytting.flytteselv.andreutgifter.parkering";
    private static final String FERGE = "flytting.flytteselv.andreutgifter.ferge";
    private static final String NAVN_FLYTTEBYRAA_1 = "flytting.flyttebyraa.forste.navn";
    private static final String BELOEP_FLYTTEBYRAA_1 = "flytting.flyttebyraa.forste.belop";
    private static final String NAVN_FLYTTEBYRAA_2 = "flytting.flyttebyraa.andre.navn";
    private static final String BELOEP_FLYTTEBYRAA_2 = "flytting.flyttebyraa.andre.belop";
    private static final String ANNET = "flytting.flytteselv.andreutgifter.annet";
    private static final String FLYTTING_FLYTTEBYRAA_VELGFORSTE = "flytting.flyttebyraa.velgforste";
    private static final String FLYTTING_DEKKET = "flytting.dekket";

    private static final Locale LOCALE = new Locale("nb", "NO");

    public static Flytteutgifter transform(WebSoknad soknad, TekstHenter tekstHenter) {
        Flytteutgifter flytteutgifter = new Flytteutgifter();
        flytteutgifter.setFlyttingPgaAktivitet(hentFlytting(soknad, "aktivitet"));
        flytteutgifter.setFlyttingPgaNyStilling(hentFlytting(soknad, "nyjobb"));
        flytteutgifter.setTiltredelsesdato(extractValue(soknad.getFaktumMedKey(NYJOBB_STARTDATO), XMLGregorianCalendar.class));
        flytteutgifter.setFlyttedato(extractValue(soknad.getFaktumMedKey(FLYTTEDATO), XMLGregorianCalendar.class));
        flytteutgifter.setTilflyttingsadresse(hentAdresse(soknad));

        flytteutgifter.setFlytterSelv(flytterSelv(soknad, tekstHenter));

        flytteutgifter.setAvstand(extractValue(soknad.getFaktumMedKey(FLYTTEAVSTAND), BigInteger.class));
        flytteutgifter.setSumTilleggsutgifter(sumDouble(soknad.getFaktumMedKey(HENGERLEIE),
                soknad.getFaktumMedKey(BOM),
                soknad.getFaktumMedKey(PARKERING),
                soknad.getFaktumMedKey(FERGE),
                soknad.getFaktumMedKey(ANNET)));
        flytteutgifter.setErUtgifterTilFlyttingDekketAvAndreEnnNAV(extractValue(soknad.getFaktumMedKey(FLYTTING_DEKKET), Boolean.class));
        if (!StofoKodeverkVerdier.FlytterSelv.flytterselv.cms.equals(flytteutgifter.getFlytterSelv())) {
            transformAnbud(soknad, flytteutgifter);
        }

        return flytteutgifter;
    }

    private static String flytterSelv(WebSoknad soknad, TekstHenter tekstHenter) {
        try {
            StofoKodeverkVerdier.FlytterSelv flytterSelv = StofoKodeverkVerdier.FlytterSelv.valueOf(extractValue(soknad.getFaktumMedKey("flytting.selvellerbistand"), String.class));
            return cms(flytterSelv.cms, tekstHenter);
        }catch(Exception ignore){
            return null;
        }
    }

    private static String flyttebyraaFaktum(WebSoknad soknad) {
        Boolean valgtForste = extractValue(soknad.getFaktumMedKey(FLYTTING_FLYTTEBYRAA_VELGFORSTE), Boolean.class);
        return isTrue(valgtForste) ? NAVN_FLYTTEBYRAA_1 : NAVN_FLYTTEBYRAA_2;
    }

    private static void transformAnbud(WebSoknad soknad, Flytteutgifter flytteutgifter) {
        Anbud anbud1 = lagAnbud(soknad, NAVN_FLYTTEBYRAA_1, BELOEP_FLYTTEBYRAA_1);
        Anbud anbud2 = lagAnbud(soknad, NAVN_FLYTTEBYRAA_2, BELOEP_FLYTTEBYRAA_2);
        List<Anbud> anbud = flytteutgifter.getAnbud();
        anbud.add(anbud1);
        anbud.add(anbud2);
        flytteutgifter.setValgtFlyttebyraa(extractValue(soknad.getFaktumMedKey(flyttebyraaFaktum(soknad)), String.class));
    }

    private static Anbud lagAnbud(WebSoknad soknad, String key, String key1) {
        String anbud1navn = extractValue(soknad.getFaktumMedKey(key), String.class);
        BigInteger anbud1Beloep = extractValue(soknad.getFaktumMedKey(key1), BigInteger.class);

        Anbud anbud = new Anbud();
        anbud.setFirmanavn(anbud1navn);
        anbud.setTilbudsbeloep(anbud1Beloep);

        return anbud;
    }

    private static String hentAdresse(WebSoknad soknad) {
        if(isTrue(extractValue(soknad.getFaktumMedKey("flytting.tidligere.riktigadresse"), Boolean.class))){
            return soknad.getFaktumMedKey("personalia").getProperties().get("gjeldendeAdresse");
        } else {
            return StofoUtils.sammensattAdresse(soknad.getFaktumMedKey(ADRESSE));
        }
    }

    private static Boolean hentFlytting(WebSoknad soknad, String type) {
        String aarsak = extractValue(soknad.getFaktumMedKey(AARSAK), String.class);
        return type.equals(aarsak) ? true : null;
    }

    private static String cms(String key, TekstHenter tekstHenter) {
        return tekstHenter.finnTekst(key, null, LOCALE);
    }
}
