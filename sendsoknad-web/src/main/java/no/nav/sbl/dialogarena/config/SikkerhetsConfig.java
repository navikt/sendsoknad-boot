package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.sikkerhet.HeaderFilter;
import no.nav.sbl.dialogarena.sikkerhet.SikkerhetsAspect;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CharacterEncodingFilter;

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

}
