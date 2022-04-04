package no.nav.sbl.soknadinnsending.fillager

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.Serializer
import no.nav.soknad.arkivering.soknadsfillager.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

class FilestorageService : Filestorage {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val filesApi: FilesApi

	@Value("\${innsending.soknadsfillager.host:localhost:9042}")
	private lateinit var soknadsfillagerHost: String
	@Value("\${INNSENDING_USERNAME}")
	private lateinit var soknadsfillagerUsername: String
	@Value("\${INNSENDING_PASSWORD}")
	private lateinit var soknadsfillagerPassword: String

	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = soknadsfillagerUsername
		ApiClient.password = soknadsfillagerPassword
		filesApi = FilesApi(soknadsfillagerHost)
	}


	override fun store(innsendingId: String, files: List<FilElementDto>) {
		logger.info("$innsendingId: Storing the following files in Soknadsfillager: ${files.map { it.id }}")
		val filedata = files.map { FileData(it.id, it.content, it.createdAt) }

		filesApi.addFiles(filedata, innsendingId)
	}

	override fun get(innsendingId: String, ids: List<String>): List<FilElementDto> {
		logger.info("$innsendingId: Getting the following files from Soknadsfillager: $ids")

		return filesApi.findFilesByIds(ids, innsendingId)
			.map { FilElementDto(it.id, it.content, it.createdAt) }
	}

	override fun delete(innsendingId: String, ids: List<String>) {
		logger.info("$innsendingId: Deleting the following files from Soknadsfillager: $ids")

		filesApi.deleteFiles(ids, innsendingId)
	}
}
