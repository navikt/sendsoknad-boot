package no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.UUID;

import static no.nav.sbl.dialogarena.types.Pingable.Ping.feilet;
import static no.nav.sbl.dialogarena.types.Pingable.Ping.lyktes;

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

    @Bean
    public Pingable digdirKrrPing(@Value("${DIGDIR_KRR_PROXY_URL}") String digdirKrrProxyBaseUrl,
                             @Value("${IGDIR_KRR_PROXY_PING}") String digdirKrrProxyPing,
                             RestTemplateBuilder restTemplateBuilder) {
        return new Pingable() {
            @Override
            public Ping ping() {
                Ping.PingMetadata metadata =
                        new Ping.PingMetadata(
                                digdirKrrProxyBaseUrl+digdirKrrProxyPing,
                                "Digital Kontaktinformasjon (Dkif-drr-proxy) - E-post service, ",
                                false);
                try {
                    RestTemplate restTemplate = restTemplateBuilder
                            .rootUri(digdirKrrProxyBaseUrl)
                            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                            .build();
                    RequestEntity<Void> requestEntity = RequestEntity
                            .get(digdirKrrProxyPing)
                            .header("Nav-Call-Id", resolveCallId())
                            .build();
                    var responseEntity = restTemplate.exchange(requestEntity, EpostService.DigitalKontaktinfo.class);

                    return lyktes(metadata);
                } catch (Exception e) {
                    return feilet(metadata, e);
                }
            }
        };
    }

    private String resolveCallId() {
        String callIdFromMdc = MDCOperations.getFromMDC(MDCOperations.MDC_CALL_ID);
        return Objects.requireNonNullElseGet(callIdFromMdc, () -> UUID.randomUUID().toString());
    }

}


