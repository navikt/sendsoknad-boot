package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto;
import no.nav.sbl.soknadinnsending.innsending.Innsending;
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata;
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata;
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static no.nav.sbl.soknadinnsending.innsending.SoknadInnsendtDtoCreatorKt.createSoknadInnsendtDto;
import static no.nav.sbl.soknadinnsending.innsending.dto.HovedskjemaType.ARKIV;
import static no.nav.sbl.soknadinnsending.innsending.dto.HovedskjemaType.FULLVERSJON;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class InnsendingOgOpplastingService {
    private static final Logger logger = getLogger(InnsendingOgOpplastingService.class);

    private final SkjemaOppslagService skjemaOppslagService;
    private final Innsending innsending;
    private final Filestorage filestorage;

    @Autowired
    public InnsendingOgOpplastingService(SkjemaOppslagService skjemaOppslagService, Innsending innsending, Filestorage filestorage) {
        this.skjemaOppslagService = skjemaOppslagService;
        this.innsending = innsending;
        this.filestorage = filestorage;
    }

    public void sendSoknad(WebSoknad soknad, List<Vedlegg> vedlegg, byte[] pdf, byte[] fullSoknad) {
        List<Hovedskjemadata> hovedskjemas = createAndUploadHovedskjemas(pdf, fullSoknad);
        innsending.sendInn(createSoknadInnsendtDto(createSoknadsdata(soknad), createVedleggdata(vedlegg), hovedskjemas));
    }

    private Soknadsdata createSoknadsdata(WebSoknad soknad) {
        String tema = skjemaOppslagService.getTema(soknad.getskjemaNummer());
        String tittel = skjemaOppslagService.getTittel(soknad.getskjemaNummer());
        return new Soknadsdata(soknad.getskjemaNummer(), soknad.erEttersending(), soknad.getAktoerId(), tema, tittel);
    }

    private List<Hovedskjemadata> createAndUploadHovedskjemas(byte[] arkivPdf, byte[] fullversjonPdf) {
        List<FilElementDto> files = new ArrayList<>();

        Hovedskjemadata arkiv = new Hovedskjemadata(UUID.randomUUID().toString(), arkivPdf.length, findFileType(arkivPdf), ARKIV);
        files.add(new FilElementDto(arkiv.getId(), arkivPdf, LocalDateTime.now()));

        Hovedskjemadata fullversjon = null;
        if (fullversjonPdf != null) {
            fullversjon = new Hovedskjemadata(UUID.randomUUID().toString(), fullversjonPdf.length, findFileType(fullversjonPdf), FULLVERSJON);
            files.add(new FilElementDto(fullversjon.getId(), fullversjonPdf, LocalDateTime.now()));
        }

        filestorage.store(files);
        return asList(arkiv, fullversjon);
    }

    private String findFileType(byte[] pdf) {
        try {
            if (PdfUtilities.erPDFA(pdf)) {
                return "PDFA";
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
                        v.getFillagerReferanse(), v.getSkjemaNummer(), finnVedleggsnavn(v), v.getStorrelse(),
                        v.getFilnavn(), v.getMimetype()
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
