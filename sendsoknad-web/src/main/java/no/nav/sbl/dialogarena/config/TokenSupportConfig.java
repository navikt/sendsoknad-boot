package no.nav.sbl.dialogarena.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;

import no.nav.security.token.support.client.core.http.OAuth2HttpClient;
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
	@Primary
	OAuth2HttpClient oAuth2HttpClientMedProxy(RestTemplateBuilder restTemplateBuilder) {

	     RestTemplateBuilder builder = restTemplateBuilder.requestFactory(() -> {
	        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("webproxy.nais", 8088));
	        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	        requestFactory.setProxy(proxy);
	        return requestFactory;
		 }).additionalInterceptors(new LoggingInterceptor());

		 return new DefaultOAuth2HttpClient(builder);
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

	public static class LoggingInterceptor implements ClientHttpRequestInterceptor {

		static Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

		@Override
		public ClientHttpResponse intercept(
				HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
			LOGGER.info("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));
			ClientHttpResponse response = ex.execute(req, reqBody);
			InputStreamReader isr = new InputStreamReader(
					response.getBody(), StandardCharsets.UTF_8);
			String body = new BufferedReader(isr).lines()
					.collect(Collectors.joining("\n"));
			LOGGER.info("Response body: {}", body);
			return response;
		}
	}
}
