package no.nav.sbl.dialogarena.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.http.HttpHost;
import org.eclipse.jetty.server.ProxyCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;

import no.nav.security.token.support.client.core.OAuth2CacheFactory;
import no.nav.security.token.support.client.core.context.JwtBearerTokenResolver;
import no.nav.security.token.support.client.core.http.OAuth2HttpClient;
import no.nav.security.token.support.client.core.oauth2.ClientCredentialsTokenClient;
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService;
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient;
import no.nav.security.token.support.client.core.oauth2.TokenExchangeClient;
import no.nav.security.token.support.client.spring.oauth2.DefaultOAuth2HttpClient;
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
