package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames
import no.nav.sbl.soknadinnsending.config.createOkHttpClientWithOAuth2
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InnsendingConfig {

	@Bean
	open fun insendingService(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		soknadsmottakerHttpClient: OkHttpClient
	): SoknadApi = SoknadApi(host, soknadsmottakerHttpClient)

	@Bean
	open fun soknadsmottakerHttpClient(
		@Qualifier(SecurityServiceBeanNames.SOKNADSMOTTAKER_BEAN_NAME) tokenService: TokenService
	) = createOkHttpClientWithOAuth2(tokenService)
}
