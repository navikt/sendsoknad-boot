package no.nav.sbl.dialogarena.config;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.security.sts.client.NavStsRestClient;
import no.nav.sbl.dialogarena.sikkerhet.HeaderFilter;
import no.nav.sbl.dialogarena.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;

import no.nav.sbl.dialogarena.tokensupport.TokenService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.client.core.context.JwtBearerTokenResolver;
import no.nav.security.token.support.client.core.http.OAuth2HttpClient;
import no.nav.security.token.support.client.core.oauth2.ClientCredentialsTokenClient;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient;
import no.nav.security.token.support.client.core.oauth2.TokenExchangeClient;
import no.nav.security.token.support.client.spring.ClientConfigurationProperties;
import no.nav.security.token.support.client.spring.oauth2.DefaultOAuth2HttpClient;
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SikkerhetsConfig {

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
            @Qualifier("AzureADTokenService") TokenService azureAdTokenService,
            @Qualifier("TokenXTokenService") TokenService tokenXService ) {

        var webClient = WebClient
                .builder()
                .baseUrl(stsUrl)
                .filter((clientRequest, next) -> {
                    
                    var token = TokenUtils.hasTokenForIssuer(TokenUtils.ISSUER_TOKENX) ? tokenXService.getToken() : azureAdTokenService.getToken(); 
                    
                    var authorizedRequest = ClientRequest.from(clientRequest)
                            .header(TokenService.FSS_PROXY_AUTHORIZATION, "Bearer " + token)
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

    @Bean("AzureADTokenService")
    public TokenService azureAdTokenService(ClientConfigurationProperties clientConfigurationProperties,
            JwtBearerTokenResolver tokenResolver, RestTemplateBuilder restTemplateBuilder) {
       
        var builder = restTemplateBuilder.requestFactory(() -> {

            Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("webproxy.nais", 8088));

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(proxy);

            return requestFactory;
        });
        var httpClient = new DefaultOAuth2HttpClient(builder);

        var oauth2Service = new OAuth2AccessTokenService(tokenResolver,
                new OnBehalfOfTokenClient(httpClient), new ClientCredentialsTokenClient(httpClient),
                new TokenExchangeClient(httpClient));
        var clientProperties = clientConfigurationProperties.getRegistration().get("azuread");
        return new TokenService(clientProperties, oauth2Service);
    }
    
    @Bean("TokenXTokenService")
    public TokenService tokenXTokenService(
            ClientConfigurationProperties clientConfigurationProperties,
            OAuth2AccessTokenService oAuth2AccessTokenService
    ) {
        
        var clientProperties = clientConfigurationProperties.getRegistration().get("tokenx");
        return new TokenService(clientProperties, oAuth2AccessTokenService);
    }
}
