package no.nav.sbl.dialogarena.config;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.modig.security.sts.client.NavStsRestClient;
import no.nav.sbl.dialogarena.sikkerhet.HeaderFilter;
import no.nav.sbl.dialogarena.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;

import no.nav.sbl.dialogarena.tokensupport.TokenService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SikkerhetsConfig {
    
    public static final String SOKNAD_FSS_TOKENX_SERVICE_NAME = "SoknadFSSTokenX";
    public static final String SOKNAD_FSS_AZUREAD_SERVICE_NAME = "SoknadFSSAzureAD";

    @Bean
    public SikkerhetsAspect sikkerhet() {
        return new SikkerhetsAspect();
    }

    @Bean
    public Tilgangskontroll tilgangskontroll() {
        return new Tilgangskontroll();
    }

    @Bean
    SpringContextAccessor springContextAccessor() { return new SpringContextAccessor(); }

    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        CharacterEncodingFilter charEncodingFilter = new CharacterEncodingFilter("UTF-8", true, true);
        FilterRegistrationBean<CharacterEncodingFilter> register = new FilterRegistrationBean<CharacterEncodingFilter>();
        register.setFilter(charEncodingFilter);
        register.setAsyncSupported(true);
        register.addUrlPatterns("/*");
        register.setName("charEncodingFilter");
        return register;
    }

    @Bean
    public FilterRegistrationBean<HeaderFilter> headerFilter() {
        HeaderFilter headerFilter = new HeaderFilter();
        FilterRegistrationBean<HeaderFilter> register = new FilterRegistrationBean<HeaderFilter>();
        register.setFilter(headerFilter);
        register.setAsyncSupported(true);
        register.addUrlPatterns("/*");
        register.setName("SecurityHeaderFilter");
        return register;

    }

    @Bean
    public NavStsRestClient stsRestClient(
            @Value("${no.nav.modig.security.sts.rest.url}") String stsUrl,
            @Value("${systemuser.sendsoknad.username}") String systemUser,
            @Value("${systemuser.sendsoknad.password}") String systemPassword,
            @Value("${api-key.legacy-sts}") String apiKeyLegacySts,
            @Value("${no.nav.modig.security.sts.rest.systemSamlPath}") String systemSamlPath,
            @Value("${no.nav.modig.security.sts.rest.exchangePath}") String exchangePath,
            @Qualifier(SOKNAD_FSS_AZUREAD_SERVICE_NAME) TokenService azureAdTokenService,
            @Qualifier(SOKNAD_FSS_TOKENX_SERVICE_NAME) TokenService tokenXService ) {

        var webClient = WebClient
                .builder()
                .baseUrl(stsUrl)
                .filter((clientRequest, next) -> {
                    
                    var token = TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_TOKENX) ? tokenXService.getToken() : azureAdTokenService.getToken(); 
                    
                    var authorizedRequest = ClientRequest.from(clientRequest)
                            .header(TokenUtils.FSS_PROXY_AUTHORIZATION_HEADER, "Bearer " + token)
                            .build();

                    return next.exchange(authorizedRequest);
                })
                .build();

        var config = new NavStsRestClient.Config();
        config.systemUser = systemUser;
        config.systemPassword = systemPassword;
        config.apiKey = apiKeyLegacySts;
        config.systemSamlPath = systemSamlPath;
        config.exchangePath = exchangePath;

        return new NavStsRestClient(webClient, config);
    }

    @Bean(SOKNAD_FSS_AZUREAD_SERVICE_NAME)
    public TokenService azureAdTokenService(ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService) {
       
        var clientProperties = clientConfigurationProperties.getRegistration().get("soknad-fss-proxy-azuread");
        return new TokenService(clientProperties, oAuth2AccessTokenService);
    }
    
    @Bean(SOKNAD_FSS_TOKENX_SERVICE_NAME)
    public TokenService tokenXTokenService(
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        
        var clientProperties = clientConfigurationProperties.getRegistration().get("soknad-fss-proxy-tokenx");
        return new TokenService(clientProperties, oAuth2AccessTokenService);
    }

    @Bean("DigdirKrrProxyTokenX")
    TokenService digdirKrrProxyTokenXTokenService(
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService) {
        var clientProperties = clientConfigurationProperties.getRegistration().get("digdir-krr-proxy-tokenx");
        return new TokenService(clientProperties, oAuth2AccessTokenService);
    }
}
