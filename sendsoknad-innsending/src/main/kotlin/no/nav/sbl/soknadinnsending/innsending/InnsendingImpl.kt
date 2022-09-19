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
import org.springframework.stereotype.Service

@Service
class InnsendingImpl(
	val soknadApi: SoknadApi
) : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)



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
