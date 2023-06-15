package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad.Type.Faktum;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@Path("/fakta")
//@TODO hva skall vi gjøre med dette ? @Timed
@Produces(APPLICATION_JSON)
@ProtectedWithClaims(issuer = "tokenx", claimMap = {TokenUtils.ACR_LEVEL4, TokenUtils.ACR_IDPORTEN_LOA_HIGH})
public class FaktaRessurs {

    private static final Logger logger = getLogger(FaktaRessurs.class);


    @Autowired
    private FaktaService faktaService;
    @Autowired
    private VedleggService vedleggService;

    @POST
    @Consumes(APPLICATION_JSON)
    @SjekkTilgangTilSoknad
    public Faktum opprettFaktum(@QueryParam("behandlingsId") final String behandlingsId, Faktum faktum) {
        return faktaService.opprettBrukerFaktum(behandlingsId, faktum);
    }

    @GET
    @Path("/{faktumId}")
    @SjekkTilgangTilSoknad(type = Faktum)
    public Faktum hentFaktum(@PathParam("faktumId") final Long faktumId) {
        logger.debug("hentFaktum, faktumId={}", faktumId);
        return faktaService.hentFaktum(faktumId);
    }

    @PUT
    @Path("/{faktumId}")
    @Consumes(APPLICATION_JSON)
    @SjekkTilgangTilSoknad(type = Faktum)
    public Faktum lagreFaktum(@PathParam("faktumId") final Long faktumId, Faktum faktum) {
        if (faktumId.equals(faktum.getFaktumId())) {
            return faktaService.lagreBrukerFaktum(faktum);
        } else {
            throw new RuntimeException(format("Faktumets ID (%d) matcher ikke faktumId (%d)", faktum.getFaktumId(), faktumId)); // TODO: Kast exception med 400 og Feilmelding?
        }
    }

    // TODO: midlertidig fiks som støtter oppdatering av faktum med POST pga. av begrensning i angualar.js sin resource-impl (snakk med Steffen Tangstad)
    @POST
    @Path("/{faktumId}")
    @Consumes(APPLICATION_JSON)
    @SjekkTilgangTilSoknad(type = Faktum)
    public Faktum lagreFaktumMedPost(@PathParam("faktumId") final Long faktumId, Faktum faktum) {
        return lagreFaktum(faktumId, faktum);
    }


    @DELETE
    @Path("/{faktumId}")
    @SjekkTilgangTilSoknad(type = Faktum)
    public void slettFaktum(@PathParam("faktumId") final Long faktumId) {
        faktaService.slettBrukerFaktum(faktumId);
    }

    @GET
    @Path("/{faktumId}/vedlegg")
    @SjekkTilgangTilSoknad(type = Faktum)
    public List<Vedlegg> hentVedlegg(@PathParam("faktumId") final Long faktumId) {
        return vedleggService.hentPaakrevdeVedlegg(faktumId);
    }
}
