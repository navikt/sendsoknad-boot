package no.nav.sbl.dialogarena.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.filter.JwtTokenValidationFilter;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;

@Configuration
@EnableOAuth2Client(cacheEnabled = true)
public class TokenSupportConfig {

	 @Bean
	 public MultiIssuerProperties multiIssuerProperties () {
		 return new MultiIssuerProperties();
	 }
	
	 @Bean
	 public FilterRegistrationBean<JwtTokenValidationFilter> oidcTokenValidationFilterBean(
	            MultiIssuerConfiguration config) {
	        return new FilterRegistrationBean<>(new JaxrsJwtTokenValidationFilter(config));
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
	 
	 @Bean
	 public TokenValidationContextHolder jaxrsContextHolder() {
	     return JaxrsTokenValidationContextHolder.getHolder();
	 } 
	 
}
