package no.nav.sbl.dialogarena.integration.security;

import no.nav.sbl.dialogarena.integration.AbstractSecurityIT;
import no.nav.sbl.dialogarena.integration.EndpointDataMocking;
import no.nav.sbl.dialogarena.integration.SoknadTester;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPOrdinaerInformasjon;
import no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SoknadActionsEndpointIT extends AbstractSecurityIT {

    private static final String DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN = "04031659235"; // Ikke ekteperson
    private String skjemanummer = new AAPOrdinaerInformasjon().getSkjemanummer().get(0);

    @Before
    public void setup() throws Exception {
        EndpointDataMocking.setupMockWsEndpointData();
    }

    @Test
    public void GET_leggVedVedlegg_leggved() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/leggved";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
            webTarget.queryParam("fnr", DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN))
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void POST_sendSoknad_send() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/send";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void POST_sendEpost_fortsettsenere() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/fortsettsenere";
        Response responseMedBrukersEgenXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN))
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken("BRUKER_2_SIN_BEHANDLINGSID"))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedBrukersEgenXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

        Response responseMedStjeltXSRFToken = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN))
                .header("X-XSRF-TOKEN", soknadTester.getXhrHeader())
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(responseMedStjeltXSRFToken.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void POST_sendEpost_bekreftinnsending() {
        SoknadTester soknadTester = soknadMedDelstegstatusOpprettet(skjemanummer);
        String subUrl = "soknader/" + soknadTester.getBrukerBehandlingId() + "/actions/bekreftinnsending";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget ->
                webTarget.queryParam("fnr", DIFFERENT_USER_THAN_THE_ONE_CURRENTLY_LOGGED_IN))
                .buildPost(Entity.json(""))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    public void GET_finnOpprinneligInnsendtDato_opprinneliginnsendtdato() throws Exception {
        EndpointDataMocking.mockSendHenvendelse();
        SoknadTester soknadTester = SoknadTester.startSoknad(skjemanummer);
        String subUrl = "soknader/INNSENDTSOKNAD/actions/opprinneliginnsendtdato";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void GET_finnSisteInnsendteBehandlingsId_sistinnsendtebehandlingsid() throws Exception {
        EndpointDataMocking.mockSendHenvendelse();
        SoknadTester soknadTester = SoknadTester.startSoknad(skjemanummer);
        String subUrl = "soknader/INNSENDTSOKNAD/actions/sistinnsendtebehandlingsid";
        Response response = soknadTester.sendsoknadResource(subUrl, webTarget -> webTarget)
                .buildGet()
                .invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }
}
