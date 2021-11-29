package no.nav.sbl.soknadinnsending.fillager

import com.fasterxml.jackson.core.type.TypeReference
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto
import no.nav.sbl.soknadinnsending.rest.objectMapper
import no.nav.sbl.soknadinnsending.rest.performDeleteCall
import no.nav.sbl.soknadinnsending.rest.performGetCall
import no.nav.sbl.soknadinnsending.rest.performPostCall
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class FilestorageService : Filestorage {
	private val logger = LoggerFactory.getLogger(javaClass)

	@Value("\${innsending.soknadsfillager.host:localhost:9042}")
	private lateinit var soknadsfillagerHost: String
	@Value("\${innsending.soknadsfillager.path:/filer}")
	private lateinit var soknadsfillagerPath: String

	private val soknadsfillagerUsername = getSystemProperty("innsending.username")
	private val soknadsfillagerPassword = getSystemProperty("innsending.password")

	@PostConstruct
	fun debugPrinting() {
		logger.info("Created FilestorageService. Host: '$soknadsfillagerHost', path: '$soknadsfillagerPath', " +
			"username: '$soknadsfillagerUsername', p: '${soknadsfillagerPassword.first()}'")
	}

	override fun store(files: List<FilElementDto>) {
		val url = "$soknadsfillagerHost$soknadsfillagerPath"
		val headers = soknadsfillagerUsername to soknadsfillagerPassword

		performPostCall(files, url, headers, false)
	}

	override fun get(ids: List<String>): List<FilElementDto> {
		val url = "$soknadsfillagerHost$soknadsfillagerPath?ids=$ids"
		val headers = soknadsfillagerUsername to soknadsfillagerPassword

		val bytes = performGetCall(url, headers)

		return try {
			objectMapper.readValue(bytes, object : TypeReference<List<FilElementDto>>() {})
		} catch (e: Exception) {
			// TODO: Exception?
			emptyList()
		}
	}

	override fun delete(ids: List<String>) {
		val url = "$soknadsfillagerHost$soknadsfillagerPath?ids=$ids"
		val headers = soknadsfillagerUsername to soknadsfillagerPassword

		performDeleteCall(url, headers)
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
