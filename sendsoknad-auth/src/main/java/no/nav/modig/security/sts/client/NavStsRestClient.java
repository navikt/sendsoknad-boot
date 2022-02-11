package no.nav.modig.security.sts.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;

// See https://github.com/navikt/gandalf
public class NavStsRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(Class.class);
    private final WebClient webClient;
    private final String authHeader;
    private final String apiKey;

    public NavStsRestClient(WebClient webClient, String systemUser, String systemPassword, String apiKey) {
        this.webClient = webClient;
        this.authHeader = Base64.getEncoder().encodeToString((systemUser + ":" + systemPassword).getBytes());
        this.apiKey = apiKey;
    }

    public Response getSystemSaml() {
        try {
            return this.webClient
                    .get()
                    .uri("/rest/v1/sts/samltoken")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + authHeader)
//                    .header("x-nav-apiKey", apiKey)
                    .header("apiKey", apiKey)
                    .retrieve()
                    .bodyToMono(Response.class)
                    .block();
        } catch (WebClientResponseException ex) {
            LOG.error("ResponseBody: " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    public static class Response {
        public String access_token;
        public String issued_token_type;
        public String token_type;
        public int expires_in;

        public String decodedToken() {
            return new String(Base64.getDecoder().decode(access_token));
        }
    }
}
