package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class AlternativRepresentasjonService {
    private final WebSoknadConfig config;
    private final TekstHenter tekstHenter;


    @Autowired
    public AlternativRepresentasjonService(WebSoknadConfig config, TekstHenter tekstHenter) {
        this.config = config;
        this.tekstHenter = tekstHenter;
    }

    public List<AlternativRepresentasjon> hentAlternativeRepresentasjoner(WebSoknad soknad) {
        List<AlternativRepresentasjonTransformer> transformers =
                KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(tekstHenter, soknad);

        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
        return transformers.stream().map(transformer -> transformer.apply(soknad)).collect(toList());
    }

    public static List<XMLAlternativRepresentasjon> lagXmlFormat(List<AlternativRepresentasjon> alternativeRepresentasjoner) {
        return alternativeRepresentasjoner.stream().map(r ->
                new XMLAlternativRepresentasjon()
                        .withFilnavn(r.getFilnavn())
                        .withFilstorrelse(r.getContent().length + "")
                        .withMimetype(r.getMimetype())
                        .withUuid(r.getUuid()))
                .collect(toList());
    }
}
