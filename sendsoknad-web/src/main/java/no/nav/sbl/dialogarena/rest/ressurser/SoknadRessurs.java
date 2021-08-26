package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.meldinger.StartSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.service.HtmlGenerator;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.InnsendtSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.InnsendtSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Henvendelse;
import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.generateXsrfToken;

@Controller
@Path("/soknader")
//@TODO hva skall vi gjøre med dette ? @Timed
@Produces(APPLICATION_JSON)
public class SoknadRessurs {

    private static final Logger logger = LoggerFactory.getLogger(SoknadRessurs.class);

    public static final String XSRF_TOKEN = "XSRF-TOKEN-SOKNAD-API";

    @Autowired
    private FaktaService faktaService;
    @Autowired
    private VedleggService vedleggService;
    @Autowired
    private SoknadService soknadService;
    @Autowired
    private InnsendtSoknadService innsendtSoknadService;
    @Autowired
    private HtmlGenerator pdfTemplate;
    @Autowired
    private WebSoknadConfig webSoknadConfig;
    
   

    @GET
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadData(@PathParam("behandlingsId") String behandlingsId, @Context HttpServletResponse response) {
        logger.info("entering hetSoknadData for " + behandlingsId);
    	response.addCookie(xsrfCookie(behandlingsId));
    	WebSoknad websoknad = soknadService.hentSoknad(behandlingsId, true, false);
    	logger.info("exiting hetSoknadData for " + behandlingsId);
        return websoknad;
    }

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.kvitteringforinnsendtsoknad+json")
    @SjekkTilgangTilSoknad(type = Henvendelse)
    public InnsendtSoknad hentInnsendtSoknad(@PathParam("behandlingsId") String behandlingsId, @QueryParam("sprak") String sprak) {
        return innsendtSoknadService.hentInnsendtSoknad(behandlingsId, sprak);
    }

    @GET
    @Path("/{behandlingsId}")
    @Produces("application/vnd.oppsummering+html")
    @SjekkTilgangTilSoknad
    public String hentOppsummering(@PathParam("behandlingsId") String behandlingsId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, true);
        vedleggService.leggTilKodeverkFelter(soknad.hentPaakrevdeVedlegg());

        if (webSoknadConfig.brukerNyOppsummering(soknad.getSoknadId())) {
            return pdfTemplate.fyllHtmlMalMedInnhold(soknad);
        }
        return pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/" + soknad.getSoknadPrefix());
    }


    @POST
    @Consumes(APPLICATION_JSON)
    public Map<String, String> opprettSoknad(@QueryParam("ettersendTil") String behandlingsId, StartSoknad soknadType, @Context HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();
        String personId = SubjectHandler.getSubjectHandler().getUid();

        String opprettetBehandlingsId;
        if (behandlingsId == null) {
            opprettetBehandlingsId = soknadService.startSoknad(soknadType.getSoknadType(), personId);
        } else {
            WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(behandlingsId);
            if (soknad == null) {
                opprettetBehandlingsId = soknadService.startEttersending(behandlingsId, personId);
            } else {
                opprettetBehandlingsId = soknad.getBrukerBehandlingId();
            }
        }
        result.put("brukerBehandlingId", opprettetBehandlingsId);
        response.addCookie(xsrfCookie(opprettetBehandlingsId));
        return result;
    }

    @PUT  //TODO: Burde endres til å sende med hele objektet for å følge spec'en
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public void oppdaterSoknad(@PathParam("behandlingsId") String behandlingsId,
                               @QueryParam("delsteg") String delsteg,
                               @QueryParam("journalforendeenhet") String journalforendeenhet) {

        if (delsteg == null && journalforendeenhet == null) {
            throw new BadRequestException("Ingen queryparametre ble sendt inn.");
        }

        if (delsteg != null) {
            settDelstegStatus(behandlingsId, delsteg);
        }

        if (journalforendeenhet != null) {
            settJournalforendeEnhet(behandlingsId, journalforendeenhet);
        }
    }

    @DELETE
    @Path("/{behandlingsId}")
    @SjekkTilgangTilSoknad
    public void slettSoknad(@PathParam("behandlingsId") String behandlingsId) {
        soknadService.avbrytSoknad(behandlingsId);
    }

    @GET
    @Path("/{behandlingsId}/fakta")
    @SjekkTilgangTilSoknad
    public List<Faktum> hentFakta(@PathParam("behandlingsId") String behandlingsId) {
        return faktaService.hentFakta(behandlingsId);
    }

    @PUT
    @Path("/{behandlingsId}/fakta")
    @SjekkTilgangTilSoknad
    public void lagreFakta(@PathParam("behandlingsId") String behandlingsId, WebSoknad soknad) {
        soknad.getFakta().forEach(faktum -> faktaService.lagreBrukerFaktum(faktum));
    }

    @GET
    @Path("/{behandlingsId}/vedlegg")
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(@PathParam("behandlingsId") String behandlingsId) {
        logger.info("entering hentPaakrevdeVedlegg " + behandlingsId);
    	List<Vedlegg> vedlegg = vedleggService.hentPaakrevdeVedlegg(behandlingsId); 
    	logger.info("exiting hentPaakrevdeVedlegg " + behandlingsId);
    	return vedlegg;
    }


    private void settJournalforendeEnhet(String behandlingsId, String delsteg) {
        soknadService.settJournalforendeEnhet(behandlingsId, delsteg);
    }

    private void settDelstegStatus(String behandlingsId, String delsteg) {
        DelstegStatus delstegstatus;
        if (delsteg.equalsIgnoreCase("utfylling")) {
            delstegstatus = DelstegStatus.UTFYLLING;

        } else if (delsteg.equalsIgnoreCase("opprettet")) {
            delstegstatus = DelstegStatus.OPPRETTET;

        } else if (delsteg.equalsIgnoreCase("vedlegg")) {
            delstegstatus = DelstegStatus.SKJEMA_VALIDERT;

        } else if (delsteg.equalsIgnoreCase("oppsummering")) {
            delstegstatus = DelstegStatus.VEDLEGG_VALIDERT;

        } else {
            throw new SendSoknadException("Ugyldig delsteg sendt inn til REST-controller.");
        }
        soknadService.settDelsteg(behandlingsId, delstegstatus);
    }

    private static Cookie xsrfCookie(String behandlingId) {
        Cookie xsrfCookie = new Cookie(XSRF_TOKEN, generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        xsrfCookie.setSecure(true);
        return xsrfCookie;
    }
}
