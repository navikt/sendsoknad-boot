package no.nav.sbl.soknadinnsending.config

import no.nav.sbl.soknadinnsending.fillager.Filestorage
import no.nav.sbl.soknadinnsending.fillager.FilestorageService
import no.nav.sbl.soknadinnsending.innsending.Innsending
import no.nav.sbl.soknadinnsending.innsending.InnsendingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InnsendingSpringConfig {

	@Bean
	open fun innsending(): Innsending = InnsendingService()

	@Bean
	open fun filestorage(): Filestorage = FilestorageService()
}
