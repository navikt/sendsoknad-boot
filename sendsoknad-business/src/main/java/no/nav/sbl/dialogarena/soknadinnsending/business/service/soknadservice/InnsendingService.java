package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class InnsendingService {
    private static final Logger logger = getLogger(InnsendingService.class);

    private final SkjemaOppslagService skjemaOppslagService;
    private final Innsending innsending;

    @Autowired
    public InnsendingService(SkjemaOppslagService skjemaOppslagService, Innsending innsending) {
        this.skjemaOppslagService = skjemaOppslagService;
        this.innsending = innsending;
    }

    public void sendSoknad(WebSoknad soknad, List<Vedlegg> vedlegg, byte[] pdf, byte[] fullSoknad) {
        List<Hovedskjemadata> hovedskjemas = createHovedskjemas(soknad, pdf, fullSoknad);
        innsending.sendInn(createSoknadsdata(soknad), createVedleggdata(vedlegg), hovedskjemas);
    }

    private Soknadsdata createSoknadsdata(WebSoknad soknad) {
        String behandlingId = soknad.getBrukerBehandlingId();
        String skjemanummer = soknad.getskjemaNummer();
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        return new Soknadsdata(behandlingId, skjemanummer, soknad.erEttersending(), soknad.getAktoerId(), tema, tittel);
    }

    private List<Hovedskjemadata> createHovedskjemas(WebSoknad soknad, byte[] arkivPdf, byte[] fullversjonPdf) {
        List<Hovedskjemadata> output = new ArrayList<>();

        Hovedskjemadata arkiv = new Hovedskjemadata(soknad.getUuid(), "application/pdf", findFileType(arkivPdf));
        output.add(arkiv);

        if (fullversjonPdf != null) {
            Hovedskjemadata fullversjon = new Hovedskjemadata(UUID.randomUUID().toString(), "application/pdf-fullversjon", findFileType(fullversjonPdf));
            output.add(fullversjon);
        }

        return output;
    }

    private String findFileType(byte[] pdf) {
        try {
            if (PdfUtilities.erPDFA(pdf)) {
                return "PDF/A";
            } else if (PdfUtilities.isPDF(pdf)) {
                return "PDF";
            }
        } catch (Exception e) {
            logger.warn("Failed to determine file type", e);
        }
        return "UNKNOWN";
    }

    private List<Vedleggsdata> createVedleggdata(List<Vedlegg> vedlegg) {

        return vedlegg.stream()
                .map(v -> new Vedleggsdata(
                        v.getFillagerReferanse(), v.getSkjemaNummer(), finnVedleggsnavn(v), v.getFilnavn(), v.getMimetype()
                ))
                .collect(Collectors.toList());
    }

    private String finnVedleggsnavn(Vedlegg vedlegg) {
        String vedleggName = vedlegg.getNavn();
        if ("N6".equalsIgnoreCase(vedlegg.getSkjemaNummer()) && vedleggName != null && !"".equals(vedleggName)) {
            return vedleggName;
        }
        String skjemanummerTillegg = "";
        if (vedlegg.getSkjemanummerTillegg() != null && !"".equals(vedlegg.getSkjemanummerTillegg())) {
            skjemanummerTillegg = ": " + vedlegg.getSkjemanummerTillegg();
        }

        try {
            String skjemaNavn = skjemaOppslagService.getTittel(vedlegg.getSkjemaNummer());
            return skjemaNavn + skjemanummerTillegg;

        } catch (Exception e) {
            logger.warn("Unable to find tittel for '{}'", vedlegg.getSkjemaNummer());
            return "";
        }
    }
}
