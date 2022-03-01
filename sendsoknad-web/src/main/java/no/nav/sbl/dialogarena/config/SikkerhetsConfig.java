package no.nav.sbl.dialogarena.config;

import no.nav.modig.common.SpringContextAccessor;
import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.security.sts.client.NavStsRestClient;
import no.nav.sbl.dialogarena.sikkerhet.HeaderFilter;
import no.nav.sbl.dialogarena.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;
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
            @Value("${no.nav.modig.security.sts.rest.exchangePath}") String exchangePath) {

        var webClient = WebClient
                .builder()
                .baseUrl(stsUrl)
                .build();

        var config = new NavStsRestClient.Config();
        config.systemUser = systemUser;
        config.systemPassword = systemPassword;
        config.apiKey = apiKeyLegacySts;
        config.systemSamlPath = systemSamlPath;
        config.exchangePath = exchangePath;

        return new NavStsRestClient(webClient, config);
    }
}
