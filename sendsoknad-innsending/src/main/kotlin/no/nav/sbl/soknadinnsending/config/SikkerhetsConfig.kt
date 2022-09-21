package no.nav.sbl.soknadinnsending.config

import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames.SOKNADSFILLAGER_BEAN_NAME
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames.SOKNADSMOTTAKER_BEAN_NAME
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

object SecurityServiceBeanNames {
	const val SOKNADSFILLAGER_BEAN_NAME = "SoknadsfillagerAzureAD"
	const val SOKNADSMOTTAKER_BEAN_NAME = "SoknadsmottakerAzureAD"
}

@Configuration
open class SikkerhetsConfig {

  @Bean(SOKNADSMOTTAKER_BEAN_NAME)
	open fun soknadsmottakerTokenService(
		clientConfigurationProperties: ClientConfigurationProperties,
		oAuth2AccessTokenService: OAuth2AccessTokenService?
	): TokenService {
		val clientProperties: no.nav.security.token.support.client.core.ClientProperties? =
			clientConfigurationProperties.registration["soknadsmottaker"]
		return TokenService(clientProperties, oAuth2AccessTokenService)
	}

	@Bean(SOKNADSFILLAGER_BEAN_NAME)
	open fun soknadsfillagerTokenService(
		clientConfigurationProperties: ClientConfigurationProperties,
		oAuth2AccessTokenService: OAuth2AccessTokenService?
	): TokenService {
		val clientProperties: no.nav.security.token.support.client.core.ClientProperties? =
			clientConfigurationProperties.registration["soknadsfillager"]
		return TokenService(clientProperties, oAuth2AccessTokenService)
	}
}

fun createOkHttpClientWithOAuth2(tokenService: TokenService): OkHttpClient {
	return OkHttpClient().newBuilder()
		.callTimeout(1, TimeUnit.MINUTES)
		.addInterceptor {

			val token = tokenService.token
			println("Token: $token")
			val bearerRequest = it.request().newBuilder().headers(it.request().headers)
				.header("Authorization", "Bearer $token").build()

			it.proceed(bearerRequest)
		}.build()
}
