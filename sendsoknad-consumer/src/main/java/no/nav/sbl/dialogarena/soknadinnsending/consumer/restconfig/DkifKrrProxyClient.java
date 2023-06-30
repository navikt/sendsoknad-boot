package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

public class DkifKrrProxyClient {

    @Bean("digdirKrrProxyRestTemplate")
    RestTemplate digdirKrrProxyRestTemplate(
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService,
            @Value("${DIGDIR_KRR_PROXY_URL}") String digdirKrrProxyBaseUrl,
            RestTemplateBuilder restTemplateBuilder) {
        var oauth2ClientProperties = clientConfigurationProperties.getRegistration().get("digdir-krr-proxy-tokenx");
        return restTemplateBuilder
                .rootUri(digdirKrrProxyBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .additionalInterceptors(createOAuth2Interceptor(oAuth2AccessTokenService, oauth2ClientProperties))
                .build();
    }

    private ClientHttpRequestInterceptor createOAuth2Interceptor(
            OAuth2AccessTokenService oAuth2AccessTokenService,
            ClientProperties oauth2ClientProperties) {

        return (request, body, execution) -> {
            var accessTokenResponse = oAuth2AccessTokenService.getAccessToken(oauth2ClientProperties);
            request.getHeaders().setBearerAuth(accessTokenResponse.getAccessToken());
            return execution.execute(request, body);
        };
    }

}
