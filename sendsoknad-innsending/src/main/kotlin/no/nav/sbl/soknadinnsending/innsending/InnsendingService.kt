package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.SoknadInnsendtDto
import no.nav.sbl.soknadinnsending.rest.performPostCall
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class InnsendingService : Innsending {
	private val logger = LoggerFactory.getLogger(javaClass)

	@Value("\${innsending.soknadsmottaker.host:localhost:8090}")
	private lateinit var soknadsmottakerHost: String
	@Value("\${innsending.soknadsmottaker.path:/save}")
	private lateinit var soknadsmottakerPath: String

	private val soknadsmottakerUsername = getSystemProperty("innsending.username")
	private val soknadsmottakerPassword = getSystemProperty("innsending.password")

	@PostConstruct
	fun debugPrinting() {
		logger.info("Created ArchiverService. Host: '$soknadsmottakerHost', path: '$soknadsmottakerPath', " +
			"username: '$soknadsmottakerUsername', p: '${soknadsmottakerPassword.first()}'")
	}


	override fun sendInn(data: SoknadInnsendtDto) {
		val url = "$soknadsmottakerHost$soknadsmottakerPath"
		val headers = listOf(
			soknadsmottakerUsername to soknadsmottakerPassword,
			"innsendingKey" to "key" // TODO
		)

		performPostCall(data, url, headers, async = false)
	}


	private fun getSystemProperty(property: String): String {
		return try {
			System.getProperty(property)
		} catch (e: Exception) {
			logger.error("Error getting system property '$property'", e)
			""
		}
	}
}
