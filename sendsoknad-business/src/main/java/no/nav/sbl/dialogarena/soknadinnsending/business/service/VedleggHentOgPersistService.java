package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class VedleggHentOgPersistService {
    private static final Logger logger = getLogger(VedleggHentOgPersistService.class);

    private final VedleggRepository vedleggRepository;


    @Autowired
    public VedleggHentOgPersistService(VedleggRepository vedleggRepository) {
        this.vedleggRepository = vedleggRepository;
    }

    public void persisterVedlegg(String behandlingsId, List<Vedlegg> vedlegg) {
        for (Vedlegg v : vedlegg) {
            medKodeverk(v);
            vedleggRepository.opprettEllerEndreVedlegg(behandlingsId, v, null);
        }
    }


    public static void medKodeverk(Vedlegg vedlegg) {
        try {
            String skjemanummer = vedlegg.getSkjemaNummer().replaceAll("\\|.*", "");
            vedlegg.leggTilURL("URL", SkjemaOppslagService.getUrl(skjemanummer));
            vedlegg.setTittel(SkjemaOppslagService.getTittel(skjemanummer));

        } catch (Exception e) {
            String skjemanummer = vedlegg != null ? vedlegg.getSkjemaNummer() : null;
            logger.warn("Tried to set Tittel/URL for Vedlegg with skjemanummer '" + skjemanummer +
                    "', but got exception. Ignoring exception and continuing...", e);
        }
    }
}
