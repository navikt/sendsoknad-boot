package no.nav.sbl.soknadinnsending.innsending

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.Serializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

class InnsendingImpl : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val soknadApi: SoknadApi

	@Value("\${innsending.soknadsmottaker.host:localhost:8090}")
	private lateinit var soknadsmottakerHost: String
	@Value("\${INNSENDING_USERNAME}")
	private lateinit var soknadsmottakerUsername: String
	@Value("\${INNSENDING_PASSWORD}")
	private lateinit var soknadsmottakerPassword: String

	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = soknadsmottakerUsername
		ApiClient.password = soknadsmottakerPassword
		soknadApi = SoknadApi(soknadsmottakerHost)

		logger.info("Config for Soknadsmottaker. Username: $soknadsmottakerUsername, password: ${soknadsmottakerPassword[0]}, host: $soknadsmottakerHost")
	}

	override fun sendInn(
		soknadsdata: Soknadsdata,
		vedleggsdata: Collection<Vedleggsdata>,
		hovedskjemas: Collection<Hovedskjemadata>
	) {
		val soknad = createSoknad(soknadsdata, vedleggsdata, hovedskjemas)
		logger.info("${soknad.innsendingId}: Sending in Soknad to Soknadsmottaker")

		soknadApi.receive(soknad)
	}
}
