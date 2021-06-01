package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;
import no.nav.sbl.dialogarena.sendsoknad.domain.Steg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.refusjondagligreise.RefusjonDagligreiseTilXml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.OPPSUMMERING;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Steg.SOKNAD;

public class SoknadRefusjonDagligreise extends KravdialogInformasjon {
    public static final String VEDTAKPERIODER = "vedtakperioder";

    SoknadRefusjonDagligreise() {
        super(Arrays.asList("NAV 11-12.10", "NAV 11-12.11", "NAV 00-01.01"));
    }

    @Override
    public String getSoknadTypePrefix() {
        return "soknadrefusjondagligreise";
    }

    @Override
    public String getSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return "soknadrefusjondagligreise.path";
    }

    @Override
    public String getStrukturFilnavn() {
        return "refusjondagligreise.xml";
    }

    @Override
    public String getBundleName() {
        return "refusjondagligreise";
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Arrays.asList(BOLK_PERSONALIA, VEDTAKPERIODER);
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(TekstHenter tekstHenter, WebSoknad soknad) {
        AlternativRepresentasjonTransformer tilleggsstonaderTilXml = new RefusjonDagligreiseTilXml();
        Event event = MetricsFactory.createEvent("soknad.alternativrepresentasjon.aktiv");
        event.addTagToReport("skjemanummer", soknad.getskjemaNummer());
        event.addTagToReport("soknadstype", getSoknadTypePrefix());
        event.report();
        return Collections.singletonList(tilleggsstonaderTilXml);
    }

    @Override
    public Steg[] getStegliste() {
        return new Steg[]{SOKNAD, OPPSUMMERING};
    }
}
