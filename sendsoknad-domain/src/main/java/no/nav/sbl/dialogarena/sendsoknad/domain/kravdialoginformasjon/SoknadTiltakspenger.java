package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader.TiltakspengerTilJson;

import java.util.Collections;
import java.util.List;

public class SoknadTiltakspenger extends KravdialogInformasjon {

    private static final String PREFIX = "soknadtilleggsstonader";

    public SoknadTiltakspenger() {
        super(Collections.singletonList("?")); // TODO: Hvilke skjemanummer?
    }

    @Override
    public String getSoknadTypePrefix() {
        return PREFIX;
    }

    @Override
    public String getSoknadUrlKey() {
        return PREFIX + ".path";
    }

    @Override
    public String getFortsettSoknadUrlKey() {
        return getSoknadUrlKey();
    }

    @Override
    public String getStrukturFilnavn() {
        return PREFIX + ".json";
    }

    @Override
    public String getBundleName() {
        return PREFIX;
    }

    @Override
    public List<String> getSoknadBolker(WebSoknad soknad) {
        return Collections.emptyList(); // TODO: Hvilke bolker?
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(TekstHenter tekstHenter, WebSoknad soknad) {
        return Collections.singletonList(new TiltakspengerTilJson());
    }

}
