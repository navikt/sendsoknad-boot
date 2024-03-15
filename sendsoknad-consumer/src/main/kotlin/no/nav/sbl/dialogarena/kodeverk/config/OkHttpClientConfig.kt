package no.nav.sbl.dialogarena.kodeverk.config

import okhttp3.OkHttpClient
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import java.util.concurrent.TimeUnit

@Configuration
open class OkHttpClientConfig(
) {
    @Bean
    @Qualifier("kodeverkApiClient")
    open fun kodeverkApiClient(
        @Value("\${spring.application.name}")
        applicationName: String = "sendsoknad"
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .callTimeout(62, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .addInterceptor {
                val callId = UUID.randomUUID().toString()

                val request = it.request().newBuilder().headers(it.request().headers)
                    .header("Nav-Consumer-Id", applicationName)
                    .header("Nav-Call-Id", resolveCallId())
                    .build()
                it.proceed(request)
            }.build()
    }

    private fun resolveCallId(): String {
       return try {
           MDC.get("innsendingsId")
       } catch ( ex: IllegalArgumentException) {
           UUID.randomUUID().toString()
       }
    }

}