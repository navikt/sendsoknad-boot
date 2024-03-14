package no.nav.sbl.dialogarena.kodeverk.config

import no.nav.sbl.dialogarena.kodeverk.KodeverkService
import no.nav.sbl.dialogarena.soknadinnsending.consumer.restconfig.DkifKrrProxyClient
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
open class KodeverkSpringConfig {
    @Configuration
    @Import(
        DkifKrrProxyClient::class,
        OkHttpClientConfig::class,
        KodeverkService::class
    )
    open class KodeverkRestServices

}