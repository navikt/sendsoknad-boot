package no.nav.modig.security.sts.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;

// See https://github.com/navikt/gandalf
public class NavStsRestClient {
    private static final String API_KEY_HEADER = "x-nav-apiKey";
    private static final Logger LOG = LoggerFactory.getLogger(Class.class);
    private final WebClient webClient;
    private final Config config;

    public NavStsRestClient(WebClient webClient, Config config) {
        this.webClient = webClient;
        this.config = config;
    }

    public Response getSystemSaml() {
        try {
            return this.webClient
                    .get()
                    .uri(config.systemSamlPath)
                    .header(HttpHeaders.AUTHORIZATION, createAuthHeader(config))
                    .header(API_KEY_HEADER, config.apiKey)
                    .retrieve()
                    .bodyToMono(Response.class)
                    .block();
        } catch (WebClientResponseException ex) {
            LOG.error("ResponseBody: " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    // See https://github.com/navikt/gandalf#issue-saml-token-based-on-oidc-token
    public Response exchangeForSaml(String b64EncodedToken) {
        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        body.add("requested_token_type", "urn:ietf:params:oauth:token-type:saml2");
        body.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
        body.add("subject_token", b64EncodedToken);

        try {
            return this.webClient
                    .post()
                    .uri(config.exchangePath)
                    .header(HttpHeaders.AUTHORIZATION, createAuthHeader(config))
                    .header(API_KEY_HEADER, config.apiKey)
                    .body(BodyInserters.fromFormData(body))
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
            try {
                return new String(Base64.getUrlDecoder().decode(access_token));
            }
            catch (IllegalArgumentException ex) {
                return new String(Base64.getDecoder().decode(access_token));
            }
        }
    }

    public static class Config {
        public String systemUser;
        public String systemPassword;
        public String apiKey;
        public String systemSamlPath;
        public String exchangePath;
    }

    private static String encodeAsBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    private static String createAuthHeader(Config config) {
        return "Basic " + encodeAsBase64(config.systemUser + ":" + config.systemPassword);
    }
}
