package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personinfo.PersonInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.*;
import no.nav.security.token.support.client.core.ClientProperties;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import static java.lang.System.setProperty;

@Configuration
@EnableCaching
@Import({
        PersonService.class,
        PersonInfoService.class,
        EpostService.class,
        ConsumerConfig.WsServices.class,
        PersonaliaFletter.class
})
public class ConsumerConfig {

    //Må godta så store xml-payloads pga Kodeverk postnr
    static {
        setProperty("org.apache.cxf.staxutils.innerElementCountThreshold", "70000");
    }

    @Configuration
    @Profile("!integration")
    @Import({
            PersonInfoWSConfig.class,
            ArbeidWSConfig.class,
            OrganisasjonWSConfig.class,
            BrukerProfilWSConfig.class,
            DkifWSConfig.class,
            KodeverkWSConfig.class,
            PersonWSConfig.class,
            MaalgruppeWSConfig.class,
            SakOgAktivitetWSConfig.class
    })
    public static class WsServices {
    }

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
