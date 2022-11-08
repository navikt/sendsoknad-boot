package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;

@Component
public class VedleggFraHenvendelsePopulator {

    private final VedleggRepository vedleggRepository;

    @Autowired
    public VedleggFraHenvendelsePopulator(@Qualifier("vedleggRepository") VedleggRepository vedleggRepository) {
        this.vedleggRepository = vedleggRepository;
    }

    public List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad) {
        ArrayList<Vedlegg> vedleggForventninger = new ArrayList<>(soknad.hentOpplastedeVedlegg());
        final String AAP_UTLAND_SKJEMANUMMER = new AAPUtlandetInformasjon().getSkjemanummer().get(0);
        if (!AAP_UTLAND_SKJEMANUMMER.equals(soknad.getskjemaNummer())) {
            Vedlegg kvittering = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, SKJEMANUMMER_KVITTERING);

            if (kvittering != null) {
                vedleggForventninger.add(kvittering);
            }
        }
        return vedleggForventninger;
    }
}
