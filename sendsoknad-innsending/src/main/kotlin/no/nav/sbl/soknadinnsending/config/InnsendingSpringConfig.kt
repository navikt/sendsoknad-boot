package no.nav.sbl.soknadinnsending.config

import no.nav.sbl.soknadinnsending.brukernotifikasjon.Brukernotifikasjon
import no.nav.sbl.soknadinnsending.brukernotifikasjon.BrukernotifikasjonService
import no.nav.sbl.soknadinnsending.fillager.Filestorage
import no.nav.sbl.soknadinnsending.fillager.FilestorageService
import no.nav.sbl.soknadinnsending.innsending.Innsending
import no.nav.sbl.soknadinnsending.innsending.InnsendingImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class InnsendingSpringConfig {

	@Bean
	open fun innsending(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		@Value("\${innsending.soknadsmottaker.username}") username: String,
		@Value("\${innsending.soknadsmottaker.password}") password: String
	): Innsending = InnsendingImpl(host, username, password)

	@Bean
	open fun filestorage(
		@Value("\${innsending.soknadsfillager.host}") host: String,
		@Value("\${innsending.soknadsfillager.username}") username: String,
		@Value("\${innsending.soknadsfillager.password}") password: String
	): Filestorage = FilestorageService(host, username, password)

	@Bean
	open fun brukernotifikasjon(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		@Value("\${innsending.soknadsmottaker.username}") username: String,
		@Value("\${innsending.soknadsmottaker.password}") password: String,
		@Value("\${innsending.brukernotifikasjon.host}") brukernotifikasjonIngress: String
	): Brukernotifikasjon = BrukernotifikasjonService(host, username, password, brukernotifikasjonIngress)
}
