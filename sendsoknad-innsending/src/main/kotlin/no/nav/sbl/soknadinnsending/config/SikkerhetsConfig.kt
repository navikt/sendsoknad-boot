package no.nav.sbl.soknadinnsending.config

import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import  no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames.SOKNADSFILLAGER_BEAN_NAME
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames.SOKNADSMOTTAKER_BEAN_NAME
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client

object SecurityServiceBeanNames {
	const val SOKNADSFILLAGER_BEAN_NAME = "SoknadsFillagerAzureAD"
	const val SOKNADSMOTTAKER_BEAN_NAME = "SoknadsMottakerAzureAD"
}

@Configuration
@EnableOAuth2Client
open class SikkerhetsConfig {

  @Bean(SOKNADSFILLAGER_BEAN_NAME)
	open fun soknadsMottakerTokenService(
		clientConfigurationProperties: ClientConfigurationProperties,
		oAuth2AccessTokenService: OAuth2AccessTokenService?
	): TokenService {
		val clientProperties: no.nav.security.token.support.client.core.ClientProperties? =
			clientConfigurationProperties.registration["soknadsmottaker"]
		return TokenService(clientProperties, oAuth2AccessTokenService)
	}

	@Bean(SOKNADSMOTTAKER_BEAN_NAME)
	open fun soknadsFillagerTokenService(
		clientConfigurationProperties: ClientConfigurationProperties,
		oAuth2AccessTokenService: OAuth2AccessTokenService?
	): TokenService {
		val clientProperties: no.nav.security.token.support.client.core.ClientProperties? =
			clientConfigurationProperties.registration["soknadsfillager"]
		return TokenService(clientProperties, oAuth2AccessTokenService)
	}
}
