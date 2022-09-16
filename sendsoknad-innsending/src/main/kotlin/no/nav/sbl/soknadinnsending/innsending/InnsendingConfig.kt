package no.nav.sbl.soknadinnsending.innsending

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.sbl.dialogarena.tokensupport.TokenService
import no.nav.sbl.soknadinnsending.config.SecurityServiceBeanNames
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.Serializer
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
open class InnsendingConfig {

	@Bean
	open fun insendingService(
		@Qualifier("soknadsMottakerHttpClient") soknadMottakerClient: OkHttpClient,
		@Value("\${innsending.soknadsmottaker.host}") host: String
	): SoknadApi = SoknadApi(host, soknadMottakerClient)


	@Bean
	@Qualifier("soknadsMottakerHttpClient")
	open fun soknadsMottakerHttpClient(
		@Qualifier(SecurityServiceBeanNames.SOKNADSMOTTAKER_BEAN_NAME) tokenService: TokenService
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
