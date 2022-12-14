package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.security.token.support.core.api.Protected;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.UnderBehandling;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Vedlegg;
import static org.slf4j.LoggerFactory.getLogger;

@Controller
@Path("/vedlegg/{vedleggId}")
@Produces(APPLICATION_JSON)
//@TODO hva skall vi gjøre med dette ? @Timed
public class VedleggRessurs {

    private static final Logger logger = getLogger(VedleggRessurs.class);

    protected static final Integer MAKS_TOTAL_FILSTORRELSE = 1024 * 1024 * 10; // Note! Use the same value as "nginx.ingress.kubernetes.io/proxy-body-size" in the nais yaml files!

    private final VedleggService vedleggService;
    private final SoknadService soknadService;

    public VedleggRessurs(VedleggService vedleggService, SoknadService soknadService) {
        this.vedleggService = vedleggService;
        this.soknadService = soknadService;
    }


    @GET
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public Vedlegg hentVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        return vedleggService.hentVedlegg(vedleggId, false);
    }

    @PUT
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public void lagreVedlegg(@PathParam("vedleggId") final Long vedleggId, Vedlegg vedlegg) {
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());

        vedleggService.lagreVedlegg(vedlegg);

        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:lagreVedlegg, id=" + vedleggId);
    }

    @DELETE
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public void slettVedlegg(@PathParam("vedleggId") final Long vedleggId) {
        vedleggService.slettVedlegg(vedleggId);
    }

    @GET
    @Path("/fil")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public List<Vedlegg> hentVedleggUnderBehandling(
            @PathParam("vedleggId") final Long vedleggId,
            @QueryParam("behandlingsId") final String behandlingsId
    ) {
        logger.info("{}: hentVedleggUnderBehandling, vedleggId={}", behandlingsId, vedleggId);
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());

        Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);
        List<Vedlegg> vedleggListe = vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());

        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:hentVedleggUnderBehandling, id=" + vedleggId);
        return vedleggListe;
    }

    @GET
    @Path("/fil.png")
    @Produces("image/png")
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public byte[] lagForhandsvisningForVedlegg(@PathParam("vedleggId") final Long vedleggId, @QueryParam("side") final int side) {
        Map<String, Long> tidsbruk = new HashMap<>();
        tidsbruk.put("Start", System.currentTimeMillis());

        byte[] sideData = vedleggService.lagForhandsvisning(vedleggId, side);

        tidsbruk.put("Slutt", System.currentTimeMillis());
        loggStatistikk(tidsbruk, "TIDSBRUK:lagForhandsvisningForVedlegg, id=" + vedleggId + ", side=" + side + ", størrelse=" + sideData.length);
        return sideData;
    }

    @POST
    @Path("/fil")
    @Consumes(MULTIPART_FORM_DATA)
    @SjekkTilgangTilSoknad(type = Vedlegg)
    @Protected
    public List<Vedlegg> lastOppFiler(@PathParam("vedleggId") final Long vedleggId,
                                      @QueryParam("behandlingsId") String behandlingsId,
                                      @FormDataParam("files[]") final List<FormDataBodyPart> files) {

        logger.info("{}: Will begin to upload {} files. vedleggId={}", behandlingsId, files.size(), vedleggId);
        try {
            Vedlegg forventning = vedleggService.hentVedlegg(vedleggId, false);
            logger.info("{}: LastOppFiler: for vedlegg med sjemanummer {} og navn={}", behandlingsId, forventning.getSkjemaNummer(), forventning.getNavn());

            long totalStorrelse = estimerTotalVedleggsStorrelse(behandlingsId, files, forventning);
            if (totalStorrelse > MAKS_TOTAL_FILSTORRELSE) {
                logger.info("{}: Totalstørrelse={} for vedleggId={} forsøkt lastet opp", behandlingsId, totalStorrelse, vedleggId);
                throw new OpplastingException("Kunne ikke lagre fil fordi total filstørrelse er for stor", null, "vedlegg.opplasting.feil.forStor");
            }

            List<byte[]> fileContent = files.stream().map(this::getByteArray).collect(Collectors.toList());
            return uploadFiles(behandlingsId, forventning, fileContent);

        } catch (OpplastingException e) {
            logger.warn("{}: {}", behandlingsId, e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("{}: Error when uploading files for vedleggsId={}. {}", behandlingsId, vedleggId,
                    e.getMessage(), e);
            throw e;
        }
    }

    List<Vedlegg> uploadFiles(String behandlingsId, Vedlegg forventning, List<byte[]> files) {
        validereFilformat(files, behandlingsId);
        files = konverterTilPdf(files, behandlingsId);
        return lagreVedlegg(forventning, files, behandlingsId);
    }

    private void validereFilformat(List<byte[]> files, String behandlingsId) {
        for (byte[] file : files) {

            if (PdfUtilities.isPDF(file)) {
                // Kontroller at PDF er lovlig, dvs. ikke encrypted og passordbeskyttet
                try {
                    PdfUtilities.erGyldig(file);
                } catch (Exception e) {
                    throw new UgyldigOpplastingTypeException(
                            e.getMessage(), null,
                            "opplasting.feilmelding.pdf.kryptert");
                }

            } else if (!PdfUtilities.isImage(file)) {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "opplasting.feilmelding.feiltype");
            }
        }
        logger.info("{}: Files validated OK", behandlingsId);
    }

    private List<byte[]> konverterTilPdf(List<byte[]> files, String behandlingsId) {
        return files.stream().map(file -> {
            if (PdfUtilities.isImage(file)) {
                logger.info("{}: Converting image to pdf", behandlingsId);
                return PdfUtilities.createPDFFromImage(file);
            } else
                return file;
        }).collect(Collectors.toList());
    }

    private List<Vedlegg> lagreVedlegg(Vedlegg forventning, List<byte[]> files, String behandlingsId) {
        logger.info("{}: lagreVedlegg", behandlingsId);
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);
        long soknadsId = soknad.getSoknadId();

        List<Vedlegg> res = new ArrayList<>();
        for (byte[] file : files) {

            Vedlegg vedlegg = lagVedlegg(behandlingsId, forventning, soknadsId, file);

            long id = vedleggService.lagreVedlegg(vedlegg, file, behandlingsId);
            res.add(vedleggService.hentVedlegg(id, false));
        }
        return res;
    }

    private Vedlegg lagVedlegg(String behandlingsId, Vedlegg forventning, long soknadsId, byte[] file) {

        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(null)
                .medSoknadId(soknadsId)
                .medFaktumId(forventning.getFaktumId())
                .medSkjemaNummer(forventning.getSkjemaNummer())
                .medSkjemanummerTillegg(forventning.getSkjemanummerTillegg())
                .medNavn(forventning.getNavn())
                .medStorrelse((long) file.length)
                .medFillagerReferanse(forventning.getFillagerReferanse())
                .medOpprettetDato(forventning.getOpprettetDato())
                .medInnsendingsvalg(UnderBehandling)
                .medAntallSider(PdfUtilities.finnAntallSider(file));
        vedlegg.setFilnavn(returnerFilnavnMedFiltype(behandlingsId, vedlegg, file));
        return vedlegg;
    }

    private String returnerFilnavnMedFiltype(String behandlingsId, Vedlegg vedlegg, byte[] file) {
        boolean erPdfa = PdfUtilities.erPDFA(behandlingsId, file);

        String filnavn = vedlegg.lagFilNavn();
        filnavn = StringUtils.removeEnd(filnavn, ".pdf");
        filnavn = StringUtils.removeEnd(filnavn, ".pdfa");
        return filnavn + (erPdfa ? ".pdfa" : ".pdf");
    }

    private byte[] getByteArray(FormDataBodyPart file) {
        try {
            return IOUtils.toByteArray(file.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

    private Long estimerTotalVedleggsStorrelse(String behandlingsId, List<FormDataBodyPart> files, Vedlegg forventning) {
        Long totalStorrelse = 0L;
        List<Vedlegg> alleVedlegg = vedleggService.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        for (Vedlegg vedlegg : alleVedlegg) {
            totalStorrelse += vedlegg.getStorrelse();
        }

        for (FormDataBodyPart file : files) {
            totalStorrelse += file.getValueAs(File.class).length();
        }
        return totalStorrelse;
    }

    private void loggStatistikk(Map<String, Long> tidsbruk, String context) {
        if (tidsbruk.get("Slutt") != null && tidsbruk.get("Start") != null) {
            logger.debug("{} tidsbruk : {}", context, (tidsbruk.get("Slutt") - tidsbruk.get("Start")));
        }
        tidsbruk.keySet().stream()
                .filter(key -> !key.equalsIgnoreCase("Start") && !key.equalsIgnoreCase("Slutt"))
                .forEach(key -> logger.debug("{}: {}", key, tidsbruk.get(key)));
    }
}
