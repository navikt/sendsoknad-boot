package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import no.nav.melding.virksomhet.soeknadsskjema.v1.soeknadsskjema.ReisestoenadForArbeidssoeker;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoKodeverkVerdier.FormaalKodeverk.oppfolging;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.StofoTestUtils.periodeMatcher;
import static org.junit.Assert.*;

public class ArbeidReiseTilXmlTest {

    @Test
    public void skalKonvertereFaktumStruktur() {
        WebSoknad soknad = new WebSoknad();
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.registrert").medValue("2015-01-02"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.hvorforreise").medValue(oppfolging.toString()));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reiselengde").medValue("123"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisemaal")
                .medProperty("land", "Norge").medProperty("adresse", "adresse")
                .medProperty("postnr", "1256").medProperty("utenlandskadresse", "syden"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.reisedekket").medValue("true"));

        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.drosje").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.drosje.belop").medValue("50"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport").medValue("false"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.utgift").medValue("1234"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil").medValue("true"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.bompenger").medValue("1"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.parkering").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.piggdekk").medValue("2"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.ferge").medValue("3"));
        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.kostnader.annet").medValue("4"));

        soknad.getFakta().add(new Faktum().medKey("reise.arbeidssoker.offentligtransport.egenbil.parkering.belop").medValue("30"));

        ReisestoenadForArbeidssoeker result = ArbeidReiseTilXml.transform(soknad);

        assertEquals("adresse, 1256", result.getAdresse());
        assertEquals(new BigInteger("123"), result.getAvstand());
        periodeMatcher(2015, 1, 2);
        assertTrue(calendarMatches(result.getReisedato(), 2015, 1, 2));
        assertTrue(result.isErUtgifterDekketAvAndre());
        assertEquals(oppfolging.kodeverksverdi, result.getFormaal().getValue());
        assertEquals(new BigInteger("50"), result.getAlternativeTransportutgifter().getDrosjeTransportutgifter().getBeloep());
        assertEquals(Double.valueOf(12d), result.getAlternativeTransportutgifter().getEgenBilTransportutgifter().getSumAndreUtgifter());

        assertTrue(result.getAlternativeTransportutgifter().isKanEgenBilBrukes());
        assertFalse(result.getAlternativeTransportutgifter().isKanOffentligTransportBrukes());
        assertNull(result.getAlternativeTransportutgifter().getKollektivTransportutgifter());

        soknad.getFaktumMedKey("reise.arbeidssoker.offentligtransport").medValue("true");
        result = ArbeidReiseTilXml.transform(soknad);
        assertEquals(new BigInteger("1234"), result.getAlternativeTransportutgifter().getKollektivTransportutgifter().getBeloepPerMaaned());
    }

    private static boolean calendarMatches(XMLGregorianCalendar value, int year, int month, int day) {
        return value.getYear() == year && value.getMonth() == month && value.getDay() == day;
    }
}
