package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames
import no.nav.sbl.soknadinnsending.config.createOkHttpClientWithOAuth2
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FilestorageClientConfiguration {

	@Bean
	open fun soknadsfillagerApiClient(
		@Value("\${innsending.soknadsfillager.host}") host: String,
		soknadsfillagerHttpClient: OkHttpClient
	): FilesApi = FilesApi(host, soknadsfillagerHttpClient)

	@Bean
	open fun soknadsfillagerHttpClient(
		@Qualifier(SecurityServiceBeanNames.SOKNADSFILLAGER_BEAN_NAME) tokenService: TokenService
	) = createOkHttpClientWithOAuth2(tokenService)
}
