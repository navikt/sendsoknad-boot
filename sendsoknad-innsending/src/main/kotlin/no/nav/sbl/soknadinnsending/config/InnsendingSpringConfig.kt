package no.nav.sbl.soknadinnsending.config

import no.nav.sbl.soknadinnsending.fillager.FilestorageClientConfiguration
import no.nav.sbl.soknadinnsending.fillager.FilestorageService
import no.nav.sbl.soknadinnsending.innsending.InnsendingConfig
import no.nav.sbl.soknadinnsending.innsending.InnsendingImpl
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.ApiClientConfig
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(value = [SikkerhetsConfig::class,
								 FilestorageClientConfiguration::class,
                 FilestorageService::class,
						     InnsendingConfig::class,
								 InnsendingImpl::class,
                 BrukernotifikasjonService::class,
								 ApiClientConfig::class])
open class InnsendingSpringConfig {
}
