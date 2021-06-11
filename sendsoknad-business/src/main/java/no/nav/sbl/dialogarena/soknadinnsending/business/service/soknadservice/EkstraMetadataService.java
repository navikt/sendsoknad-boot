package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadMetadata.Verdi;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;

import java.util.List;
import java.util.Map;

public class EkstraMetadataService {

    static XMLSoknadMetadata hentEkstraMetadata(WebSoknad soknad) {
        List<EkstraMetadataTransformer> transformers = KravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer())
                .getMetadataTransformers();

        XMLSoknadMetadata soknadMetadata = new XMLSoknadMetadata();

        transformers.stream()
                .map(transformer -> transformer.apply(soknad))
                .forEach(map -> {
                    for (Map.Entry<String, String> v : map.entrySet()) {
                        soknadMetadata.withVerdi(new Verdi(v.getKey(), v.getValue()));
                    }
                });

        return soknadMetadata;
    }
}
