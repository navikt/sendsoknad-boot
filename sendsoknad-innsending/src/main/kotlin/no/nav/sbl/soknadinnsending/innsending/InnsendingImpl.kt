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

class InnsendingImpl(
	@Value("\${innsending.soknadsmottaker.host}") host: String,
	@Value("\${innsending.soknadsmottaker.username}") username: String,
	@Value("\${innsending.soknadsmottaker.password}") password: String,
	@Value("\${innsending.useRealEndpointInSoknadsmottaker}") private val useRealEndpoint: Boolean
) : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val soknadApi: SoknadApi

	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = username
		ApiClient.password = password
		soknadApi = SoknadApi(host)

		logger.info("Config for Soknadsmottaker. Username: $username, password: ${password[0]}, host: $host, useRealEndpoint: $useRealEndpoint")
	}

	override fun sendInn(
		soknadsdata: Soknadsdata,
		vedleggsdata: Collection<Vedleggsdata>,
		hovedskjemas: Collection<Hovedskjemadata>
	) {
		val soknad = createSoknad(soknadsdata, vedleggsdata, hovedskjemas)
		logger.info("${soknad.innsendingId}: Sending in Soknad to Soknadsmottaker")

		if (useRealEndpoint)
			soknadApi.receive(soknad)
		else
			soknadApi.receiveTest(soknad, soknad.innsendingId, "sendsoknad")
	}
}
