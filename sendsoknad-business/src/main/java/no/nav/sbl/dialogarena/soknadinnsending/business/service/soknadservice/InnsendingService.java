package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
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
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML_VALUE;

@Service
public class InnsendingService {
    private static final Logger logger = getLogger(InnsendingService.class);

    static final String DEFAULT_VEDLEGG_NAME = "nameless";
    static final String DEFAULT_FILE_TYPE = "UNKNOWN";
    static final String DEFAULT_VEDLEGG_MIMETYPE = "application/pdf";

    private final SkjemaOppslagService skjemaOppslagService;
    private final Innsending innsending;

    @Autowired
    public InnsendingService(SkjemaOppslagService skjemaOppslagService, Innsending innsending) {
        this.skjemaOppslagService = skjemaOppslagService;
        this.innsending = innsending;
    }

    public void sendSoknad(
            WebSoknad soknad,
            List<AlternativRepresentasjon> alternativeRepresentations,
            List<Vedlegg> vedlegg,
            byte[] pdf,
            byte[] fullSoknad,
            String fullSoknadId
    ) {
        List<Hovedskjemadata> hovedskjemas = createHovedskjemas(soknad, pdf, fullSoknad, fullSoknadId, alternativeRepresentations);
        innsending.sendInn(createSoknadsdata(soknad), createVedleggdata(soknad.getBrukerBehandlingId(), vedlegg), hovedskjemas);
    }

    private Soknadsdata createSoknadsdata(WebSoknad soknad) {
        String behandlingId = soknad.getBrukerBehandlingId();
        String skjemanummer = soknad.getskjemaNummer();
        String tema = skjemaOppslagService.getTema(skjemanummer);
        String tittel = skjemaOppslagService.getTittel(skjemanummer);
        return new Soknadsdata(behandlingId, skjemanummer, soknad.erEttersending(), soknad.getAktoerId(), tema, tittel);
    }

    private List<Hovedskjemadata> createHovedskjemas(
            WebSoknad soknad,
            byte[] arkivPdf,
            byte[] fullversjonPdf,
            String fullSoknadId,
            List<AlternativRepresentasjon> alternativeRepresentations
    ) {
        String behandlingsId = soknad.getBrukerBehandlingId();
        String fileType, fileName;
        List<Hovedskjemadata> output = new ArrayList<>();

        fileType = findFileType(behandlingsId, arkivPdf);
        fileName = makeFileName(soknad.getskjemaNummer(), fileType);
        Hovedskjemadata arkiv = new Hovedskjemadata(soknad.getUuid(), "application/pdf", fileType, fileName);
        output.add(arkiv);

        if (fullversjonPdf != null) {
            fileType = "PDF/A"; /*TODO: Change to findFileType(fullversjonPdf)*/
            fileName = makeFileName(soknad.getskjemaNummer(), fileType);
            Hovedskjemadata fullversjon = new Hovedskjemadata(fullSoknadId, "application/pdf-fullversjon", fileType, fileName);
            output.add(fullversjon);
        }
        output.addAll(
                alternativeRepresentations.stream()
                        .map(altRep -> createHovedskjemadata(behandlingsId, soknad.getskjemaNummer(), altRep))
                        .collect(Collectors.toList())
        );

        return output;
    }

    private Hovedskjemadata createHovedskjemadata(String behandlingsId, String skjemanummer, AlternativRepresentasjon altRep) {
        String type = findFileType(behandlingsId, altRep.getMimetype());
        String name;
        if (altRep.getFilnavn() != null && !"".equals(altRep.getFilnavn())) {
            name = altRep.getFilnavn();
        } else {
            name = makeFileName(skjemanummer, type);
        }
        return new Hovedskjemadata(altRep.getUuid(), altRep.getMimetype(), type, name);
    }

    private String makeFileName(String skjemanummer, String fileType) {
        return skjemanummer + "." + fileType.replaceAll("[^A-Za-z]", "").toLowerCase();
    }

    private String findFileType(String behandlingsId, String mimeType) {
        if (mimeType.equals(APPLICATION_XML_VALUE)) {
            return "XML";
        } else if (mimeType.equals(APPLICATION_JSON_VALUE)) {
            return "JSON";
        } else if (mimeType.startsWith("application/pdf")) {
            return "PDF";
        } else {
            logger.warn("{}: Failed to find file type for '{}'", behandlingsId, mimeType);
            return DEFAULT_FILE_TYPE;
        }
    }

    private String findFileType(String behandlingsId, byte[] pdf) {
        try {
            if (PdfUtilities.erPDFA(pdf)) {
                return "PDF/A";
            } else if (PdfUtilities.isPDF(pdf)) {
                return "PDF";
            }
        } catch (Exception e) {
            logger.warn("{}: Failed to determine file type", behandlingsId, e);
        }
        return DEFAULT_FILE_TYPE;
    }

    private List<Vedleggsdata> createVedleggdata(String behandlingsId, List<Vedlegg> vedlegg) {

        return vedlegg.stream()
                .filter(v -> beholdOpplastedeVedlegg(behandlingsId, v))
                .map(v -> createVedleggsdata(behandlingsId, v))
                .collect(Collectors.toList());
    }

    private boolean beholdOpplastedeVedlegg(String behandlingsId, Vedlegg v) {
        if (!v.getInnsendingsvalg().er(LastetOpp)) {
            logger.info("{}: Vedlegg {} har status {}. Sender kun vedlegg med status {} til arkiv.",
                    behandlingsId, v.getSkjemaNummer(), v.getInnsendingsvalg(), LastetOpp);
        }
        return v.getInnsendingsvalg().er(LastetOpp);
    }

    private Vedleggsdata createVedleggsdata(String behandlingsId, Vedlegg v) {
        String mediatype = v.getMimetype() == null || "".equals(v.getMimetype()) ? DEFAULT_VEDLEGG_MIMETYPE : v.getMimetype();
        String name = v.lagFilNavn();
        if (name == null || "".equals(name)) {
            name = DEFAULT_VEDLEGG_NAME;
        }
        return new Vedleggsdata(
                v.getFillagerReferanse(),
                mediatype,
                findFileType(behandlingsId, mediatype),
                name,
                v.getSkjemaNummer(),
                finnVedleggsnavn(behandlingsId, v)
        );
    }

    private String finnVedleggsnavn(String behandlingsId, Vedlegg vedlegg) {
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
            logger.warn("{}: Unable to find tittel for '{}'", behandlingsId, vedlegg.getSkjemaNummer());
            return "";
        }
    }
}
