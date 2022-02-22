package no.nav.modig.security.sts.client;

import com.nimbusds.jose.util.Base64URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final String authHeader;
    private final String apiKey;

    public NavStsRestClient(WebClient webClient, String systemUser, String systemPassword, String apiKey) {
        this.webClient = webClient;
        this.authHeader = encodeAsBase64(systemUser + ":" + systemPassword);
        this.apiKey = apiKey;
    }

    public Response getSystemSaml() {
        try {
            return this.webClient
                    .get()
                    .uri("/rest/v1/sts/samltoken")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + authHeader)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(Response.class)
                    .block();
        } catch (WebClientResponseException ex) {
            LOG.error("ResponseBody: " + ex.getResponseBodyAsString());
            throw ex;
        }
    }

    // See https://github.com/navikt/gandalf#issue-saml-token-based-on-oidc-token
    public Response exchangeForSaml(String token) {
        var b64UrlEncodedToken = Base64URL.encode(token).toString();

        var body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        body.add("requested_token_type", "urn:ietf:params:oauth:token-type:saml2");
        body.add("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
        body.add("subject_token", b64UrlEncodedToken);

        LOG.info("Token: " + token);
        LOG.info("Token encoded: " + b64UrlEncodedToken);

        try {
            return this.webClient
                    .post()
                    .uri("/rest/v1/sts/token/exchange")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + authHeader)
                    .header(API_KEY_HEADER, apiKey)
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
            return new String(Base64.getDecoder().decode(access_token));
        }
    }

    private static String encodeAsBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
}
