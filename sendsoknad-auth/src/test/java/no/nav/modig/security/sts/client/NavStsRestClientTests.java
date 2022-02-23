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
import java.util.Base64;

public class NavStsRestClientTests {
    private NavStsRestClient sut;
    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        var rootUrl = server.url("").toString();
        var webClient = WebClient.builder().baseUrl(rootUrl).build();
        sut = new NavStsRestClient(webClient, "user", "psw", "test-api-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void getSystemSaml_should_handle_valid_response() throws Exception {
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
        Assertions.assertEquals("test-api-key", request.getHeader("x-nav-apiKey"));
    }
    private static String encodeAsBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    @Test
    void exchangeForSaml_should_post_valid_request() throws Exception {
        var response = new MockResponse()
                .setBody(stsJson())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        server.enqueue(response);

        var saml = sut.exchangeForSaml(encodeAsBase64("base64-test"));
        Assertions.assertTrue(saml.decodedToken().startsWith("<saml2:Assertion"));

        var request = server.takeRequest();
        Assertions.assertEquals("POST", request.getMethod());
        Assertions.assertEquals("/rest/v1/sts/token/exchange", request.getPath());
        Assertions.assertEquals("Basic dXNlcjpwc3c=", request.getHeader(HttpHeaders.AUTHORIZATION));
        Assertions.assertEquals("test-api-key", request.getHeader("x-nav-apiKey"));
        Assertions.assertEquals("application/x-www-form-urlencoded;charset=UTF-8", request.getHeader(HttpHeaders.CONTENT_TYPE));

        var body = new String(request.getBody().readByteArray());
        Assertions.assertEquals(
                "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Atoken-exchange&requested_token_type=urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Asaml2&subject_token_type=urn%3Aietf%3Aparams%3Aoauth%3Atoken-type%3Aaccess_token&subject_token=base64-test",
                body);
    }

    @Test
    void client_should_throw_on_invalid_response() {
        var response = new MockResponse()
                .setResponseCode(HttpStatus.UNAUTHORIZED.value())
                .setBody("My test error message.");

        server.enqueue(response);

        Assertions.assertThrows(WebClientResponseException.Unauthorized.class, () -> sut.getSystemSaml());
    }

    public String stsJson() throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/no/nav/modig/http-responses/sts.json")));
    }
}
