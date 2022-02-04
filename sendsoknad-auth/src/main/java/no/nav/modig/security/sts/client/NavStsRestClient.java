package no.nav.modig.security.sts.client;

import no.nav.modig.core.context.ModigSecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

// See https://github.com/navikt/gandalf
@Component
public class NavStsRestClient {
    private final WebClient webClient;
    private final String authHeader;

    public NavStsRestClient(
            WebClient.Builder webClientBuilder,
            @Value("${no.nav.modig.security.rest.url}") String stsUrl,
            @Value("${" + ModigSecurityConstants.SYSTEMUSER_USERNAME + "}") String systemUser,
            @Value("${" + ModigSecurityConstants.SYSTEMUSER_PASSWORD + "}") String systemPassword) {
        this.webClient = webClientBuilder.baseUrl(stsUrl).build();
        this.authHeader = Base64.getEncoder().encodeToString((systemUser + ":" + systemPassword).getBytes());
    }

    public Response getSystemSaml() {
        return this.webClient
                .get()
                .uri("/rest/v1/sts/samltoken")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + authHeader)
                .retrieve()
                .bodyToMono(Response.class)
                .block();
    }

    static class Response {
        public String access_token;
        public String issued_token_type;
        public String token_type;
        public int expires_in;

        String decodedToken() {
            return new String(Base64.getDecoder().decode(access_token));
        }
    }
}
