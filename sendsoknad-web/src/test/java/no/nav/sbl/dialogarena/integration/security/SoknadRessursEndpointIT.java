package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadRessursEndpointIT extends AbstractSecurityIT {
    public static final String ANNEN_BRUKER = "12345678901";
    private String skjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void GET_hentFakta_behandlingsId_fakta() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);

        String url = "soknader/" + soknadTester.getBrukerBehandlingId() + "/fakta";
        Response response = soknadTester.sendsoknadResource(url, webTarget ->
                webTarget.queryParam("fnr", ANNEN_BRUKER))
                .buildGet()
                .invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }
}
