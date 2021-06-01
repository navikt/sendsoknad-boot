package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Boutgifter;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Periode;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.math.BigInteger;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.faktumTilPeriode;

public class BoutgifterTilXml {

    public static final String AKTIVITETSADRESSE = "bostotte.adresseutgifter.aktivitetsadresse";
    public static final String HJEMSTEDSADDRESSE = "bostotte.adresseutgifter.hjemstedsaddresse";
    public static final String OPPHORTE = "bostotte.adresseutgifter.opphorte";
    public static final String SAMLING = "bostotte.samling";
    public static final String KOMMUNESTOTTE = "bostotte.kommunestotte";
    public static final String UTGIFT = "utgift";
    public static final String AARSAK = "bostotte.aarsak";
    public static final String PERIODE = "bostotte.periode";

    public static Boutgifter transform(WebSoknad webSoknad) {
        Boutgifter boutgifter = new Boutgifter();
        aarsakTilBoutgifter(webSoknad, boutgifter);
        boutgifter.setPeriode(faktumTilPeriode(webSoknad.getFaktumMedKey(PERIODE)));

        String kommuneFlag = webSoknad.getFaktaMedKey(KOMMUNESTOTTE).toString();

        if (kommuneFlag.length() > 2 ) {
            kommunestotteTilBoutgifter(webSoknad, boutgifter);
        }
        samlingTilBoutgifter(webSoknad, boutgifter);
        adresseUtgifterTilBoutgifter(webSoknad, boutgifter);

        return boutgifter;
    }

    private static void adresseUtgifterTilBoutgifter(WebSoknad webSoknad, Boutgifter boutgifter) {
        Faktum aktivitetstedFaktum = webSoknad.getFaktumMedKey(AKTIVITETSADRESSE);

        if (aktivitetstedFaktum != null && aktivitetstedFaktum.hasEgenskap(UTGIFT) && "true".equals(aktivitetstedFaktum.getValue())) {
            boutgifter.setBoutgifterAktivitetsted(new BigInteger(aktivitetstedFaktum.getProperties().get(UTGIFT)));
        }

        Faktum hjemstedsaddresse = webSoknad.getFaktumMedKey(HJEMSTEDSADDRESSE);
        if (hjemstedsaddresse != null && hjemstedsaddresse.hasEgenskap(UTGIFT) && "true".equals(hjemstedsaddresse.getValue())) {
            boutgifter.setBoutgifterHjemstedAktuell(new BigInteger(hjemstedsaddresse.getProperties().get(UTGIFT)));
        }

        Faktum opphorte = webSoknad.getFaktumMedKey(OPPHORTE);
        if (opphorte != null && opphorte.hasEgenskap(UTGIFT) && "true".equals(opphorte.getValue())) {
            boutgifter.setBoutgifterHjemstedOpphoert(new BigInteger(opphorte.getProperties().get(UTGIFT)));
        }
    }

    private static void samlingTilBoutgifter(WebSoknad webSoknad, Boutgifter boutgifter) {
        List<Faktum> samlingFakta = webSoknad.getFaktaMedKey(SAMLING);
        if (samlingFakta != null) {
            for (Faktum samlingFaktum : samlingFakta) {
                Periode periode = faktumTilPeriode(samlingFaktum);
                if(periode != null) {
                    boutgifter.getSamlingsperiode().add(periode);
                }
            }
        }
    }

    private static void kommunestotteTilBoutgifter(WebSoknad webSoknad, Boutgifter boutgifter) {
        Faktum kommunestotteFaktum = webSoknad.getFaktumMedKey(KOMMUNESTOTTE);

        if (kommunestotteFaktum != null && kommunestotteFaktum.hasValue()) {
            boutgifter.setMottarBostoette(Boolean.valueOf(kommunestotteFaktum.getValue()));
            if (boutgifter.isMottarBostoette() != null && boutgifter.isMottarBostoette()) {
                if ( !kommunestotteFaktum.getProperties().isEmpty() ) {
                    boutgifter.setBostoetteBeloep(new BigInteger(kommunestotteFaktum.getProperties().get(UTGIFT)));
                }
            }
        }
    }

    private static void aarsakTilBoutgifter(WebSoknad webSoknad, Boutgifter boutgifter) {
        Faktum aarsakFaktum = webSoknad.getFaktumMedKey(AARSAK);
        if (aarsakFaktum != null && aarsakFaktum.hasValue()) {
            boutgifter.setHarFasteBoutgifter("fasteboutgifter".equals(aarsakFaktum.getValue()));
            boutgifter.setHarBoutgifterVedSamling("samling".equals(aarsakFaktum.getValue()));
        }
    }
}
