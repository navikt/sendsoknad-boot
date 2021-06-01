package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReiseOppstartOgAvsluttetAktivitet;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoUtils;

import java.math.BigInteger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.faktumTilPeriode;

public class ReiseOppstartOgAvsluttetAktivitetTilXml {

    private static final String FAKTUM_REISEMAAL = "reise.midlertidig.reisemaal";
    private static final String FAKTUM_PERIODE = "reise.midlertidig.periode";
    private static final String FAKTUM_AVSTAND = "reise.midlertidig.reiselengde";
    private static final String FAKTUM_HJEMMEBOENDE = "reise.midlertidig.hjemmeboende";
    private static final String FAKTUM_ANTALL_REISER = "reise.midlertidig.antallreiser";

    public static ReiseOppstartOgAvsluttetAktivitet transform(WebSoknad soknad) {
        ReiseOppstartOgAvsluttetAktivitet reise = new ReiseOppstartOgAvsluttetAktivitet();
        reise.setPeriode(faktumTilPeriode(soknad.getFaktumMedKey(FAKTUM_PERIODE)));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey(FAKTUM_AVSTAND), BigInteger.class));
        reise.setAktivitetsstedAdresse(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey(FAKTUM_REISEMAAL)));
        reise.setHarBarnUnderFemteklasse(extractValue(soknad.getFaktumMedKey(FAKTUM_HJEMMEBOENDE), Boolean.class));
        reise.setHarBarnUnderAtten(harBarnUnder18(soknad));
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "midlertidig"));
        reise.setAntallReiser(extractValue(soknad.getFaktumMedKey(FAKTUM_ANTALL_REISER), BigInteger.class));
        return reise;
    }

    private static Boolean harBarnUnder18(WebSoknad soknad) {
        for (Faktum faktum : soknad.getFaktaMedKey("barn")) {
            if (faktum.harPropertySomMatcher("skalFlytteMed", "true")) {
                return true;
            }
        }
        return false;
    }
}
