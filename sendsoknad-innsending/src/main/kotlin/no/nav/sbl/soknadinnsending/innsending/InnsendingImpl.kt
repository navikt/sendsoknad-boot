package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InnsendingImpl(private val soknadApi: SoknadApi) : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)


	override fun sendInn(
		soknadsdata: Soknadsdata,
		vedleggsdata: Collection<Vedleggsdata>,
		hovedskjemas: Collection<Hovedskjemadata>
	) {
		val soknad = createSoknad(soknadsdata, vedleggsdata, hovedskjemas)
		logger.info("${soknad.innsendingId}: Sending in Soknad to Soknadsmottaker")

		soknadApi.receive(soknad,"disabled")
	}
}
