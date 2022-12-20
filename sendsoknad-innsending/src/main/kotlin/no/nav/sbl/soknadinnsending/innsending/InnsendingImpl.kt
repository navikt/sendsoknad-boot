package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class InnsendingImpl(private val soknadApi: SoknadApi) : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)


	override fun sendInn(
		soknadsdata: Soknadsdata,
		vedleggsdata: Collection<Vedleggsdata>,
		hovedskjemas: Collection<Hovedskjemadata>
	) {
		val soknad = createSoknad(soknadsdata, vedleggsdata, hovedskjemas)
		val time = measureTimeMillis {
			soknadApi.receive(soknad)
		}
		logger.info("${soknad.innsendingId}: Sent Soknad to Soknadsmottaker in $time ms.")
	}
}
