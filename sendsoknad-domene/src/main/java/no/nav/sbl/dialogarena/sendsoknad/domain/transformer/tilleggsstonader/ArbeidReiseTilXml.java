package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.Formaal;
import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReisestoenadForArbeidssoeker;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.StofoTransformers.extractValue;

public class ArbeidReiseTilXml {

    public static ReisestoenadForArbeidssoeker transform(WebSoknad soknad) {
        ReisestoenadForArbeidssoeker reise = new ReisestoenadForArbeidssoeker();
        reise.setReisedato(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.registrert"), XMLGregorianCalendar.class));
        reise.setFormaal(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.hvorforreise"), Formaal.class));
        reise.setAdresse(StofoUtils.sammensattAdresse(soknad.getFaktumMedKey("reise.arbeidssoker.reisemaal")));
        reise.setAvstand(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reiselengde"), BigInteger.class));
        reise.setErUtgifterDekketAvAndre(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.reisedekket"), Boolean.class));
        reise.setErVentetidForlenget(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.dagpenger.forlenget"), Boolean.class));
        reise.setFinnesTidsbegrensetbortfall(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.dagpenger.bortfall"), Boolean.class));
        reise.setAlternativeTransportutgifter(StofoUtils.alternativeTransportUtgifter(soknad, "arbeidssoker"));
        reise.setHarMottattDagpengerSisteSeksMaaneder(extractValue(soknad.getFaktumMedKey("reise.arbeidssoker.dagpenger"), Boolean.class));
        return reise;
    }
}
