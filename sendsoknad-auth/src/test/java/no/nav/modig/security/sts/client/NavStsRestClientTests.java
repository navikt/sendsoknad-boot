package no.nav.modig.security.sts.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NavStsRestClientTests {
    private NavStsRestClient sut;
    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        var rootUrl = server.url("").toString();
        sut = new NavStsRestClient(WebClient.builder(), rootUrl, "user", "psw");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void client_should_handle_valid_response() throws Exception {
        var response = new MockResponse()
                .setBody(stsJson())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        server.enqueue(response);

        var saml = sut.getSystemSaml();
        Assertions.assertTrue(saml.decodedToken().startsWith("<saml2:Assertion"));

        var request = server.takeRequest();
        Assertions.assertEquals("GET", request.getMethod());
        Assertions.assertEquals("/rest/v1/sts/samltoken", request.getPath());
        Assertions.assertEquals("Basic dXNlcjpwc3c=", request.getHeader(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void client_should_throw_on_invalid_response() {
        var response = new MockResponse()
                .setResponseCode(HttpStatus.UNAUTHORIZED.value());

        server.enqueue(response);

        Assertions.assertThrows(WebClientResponseException.Unauthorized.class, () -> sut.getSystemSaml());
    }

    public String stsJson() throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/no/nav/modig/http-responses/sts.json")));
    }
}
