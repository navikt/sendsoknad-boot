package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.sendsoknad.domain.health.ApplicationStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.health.ApplicationStatusType;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/health")
@Produces(APPLICATION_JSON)
@Unprotected
public class HealthCheck {
    @GET()
    @Path("/status")
    public ApplicationStatus getStatus() {
        return new ApplicationStatus(ApplicationStatusType.OK, "OK", null);
    }
}
