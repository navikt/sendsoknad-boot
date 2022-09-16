package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames
import no.nav.sbl.soknadinnsending.config.SikkerhetsConfig
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
open class FilestorageClientConfiguration {

	@Bean
	open fun soknadFillagerApiClient(
		@Value("\${innsending.soknadsfillager.host}") host: String,
		soknadsFillagerHttpClient: OkHttpClient
	): FilesApi = FilesApi(host, soknadsFillagerHttpClient)

	@Bean
	open fun soknadsFillagerHttpClient(
		@Qualifier(SecurityServiceBeanNames.SOKNADSFILLAGER_BEAN_NAME) tokenService: TokenService
	): OkHttpClient {

		return OkHttpClient().newBuilder()
			.callTimeout(1, TimeUnit.MINUTES)
			.addInterceptor {

				val token = tokenService.getToken()
				val bearerRequest = it.request().newBuilder().headers(it.request().headers)
					.header("Authorization", "Bearer $token").build()

				it.proceed(bearerRequest)
			}.build()
	}


}
