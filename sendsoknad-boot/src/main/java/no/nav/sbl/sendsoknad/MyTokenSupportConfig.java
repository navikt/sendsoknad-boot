package no.nav.sbl.sendsoknad;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextListener;

import no.nav.sbl.dialogarena.config.MultiIssuerProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import no.nav.security.token.support.filter.JwtTokenValidationFilter;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;

public class MyTokenSupportConfig {

    @Bean
    public MultiIssuerProperties multiIssuerProperties () {
            return new MultiIssuerProperties();
    }
   
    @Bean
    public FilterRegistrationBean<JwtTokenValidationFilter> oidcTokenValidationFilterBean(
               MultiIssuerConfiguration config) {
          FilterRegistrationBean<JwtTokenValidationFilter> registration = new FilterRegistrationBean<JwtTokenValidationFilter>(new JaxrsJwtTokenValidationFilter(config));
          registration.setOrder(Integer.MAX_VALUE-1);
          return registration;
    }
    
    
    @Bean
    public MultiIssuerConfiguration multiIssuerConfiguration(MultiIssuerProperties issuerProperties,ProxyAwareResourceRetriever resourceRetriever) {
           return new MultiIssuerConfiguration(issuerProperties.getIssuer(), resourceRetriever);
    }
    
    
    @Bean
    public ProxyAwareResourceRetriever oidcResourceRetriever() {
        return new ProxyAwareResourceRetriever();
    }
    

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
    
}
