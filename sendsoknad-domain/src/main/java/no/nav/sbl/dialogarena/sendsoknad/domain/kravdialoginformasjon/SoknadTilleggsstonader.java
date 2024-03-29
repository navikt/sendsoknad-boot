package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.TilleggsstonaderTilXml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SoknadTilleggsstonader extends KravdialogInformasjon {

    public SoknadTilleggsstonader() {
        super(Arrays.asList("NAV 11-12.12", "NAV 11-12.13", "NAV 11-12.14"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "soknadtilleggsstonader";
    }

    @Override
    public String getSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "soknadtilleggsstonader.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "soknadtilleggsstonader.xml";
    }

    @Override
    public String getBundleName() {
        return "soknadtilleggsstonader";
    }

    @Override
    public String getTema() {
        return "TSO";
    }

    @Override
    public String getTema(String skjemanr) {
        if ("NAV 11-12.13".equalsIgnoreCase(skjemanr)) return "TSR";
        else return "TSO";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA, BOLK_BARN);
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(TekstHenter tekstHenter, WebSoknad soknad) {
        AlternativRepresentasjonTransformer tilleggsstonaderTilXml = new TilleggsstonaderTilXml(tekstHenter);
     // @TODO hva skall vi gjøre med dette ?
       /* Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report(); */
        return Collections.singletonList(tilleggsstonaderTilXml);
    }
}
