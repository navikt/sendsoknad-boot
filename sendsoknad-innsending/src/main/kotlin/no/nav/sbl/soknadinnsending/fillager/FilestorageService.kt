package no.nav.sbl.soknadinnsending.fillager

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsfillager.infrastructure.Serializer
import no.nav.soknad.arkivering.soknadsfillager.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

class FilestorageService(
	@Value("\${innsending.soknadsfillager.host}") host: String,
	@Value("\${innsending.soknadsfillager.username}") username: String,
	@Value("\${innsending.soknadsfillager.password}") password: String
) : Filestorage {
	private val logger = LoggerFactory.getLogger(javaClass)
	private val filesApi: FilesApi

	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = username
		ApiClient.password = password
		filesApi = FilesApi(host)

		logger.info("Config for Soknadsfillager. Username: $username, password: ${password[0]}, host: $host")
	}


	override fun store(innsendingId: String, files: List<FilElementDto>) {
		logger.info("$innsendingId: Storing the following files in Soknadsfillager: ${files.map { it.id }}")
		val filedata = files.map { FileData(it.id, it.content, it.createdAt) }

		filesApi.addFiles(filedata , innsendingId)
	}

  override fun getFileMetadata(innsendingId: String, ids: List<String>) :List<FileData> {
		logger.info("$innsendingId: Getting metadata for the following files from Soknadsfillager: $ids")
		return filesApi.findFilesByIds(ids,true,innsendingId)
	}

	override fun getFiles(innsendingId: String, ids: List<String>): List<FilElementDto> {
		logger.info("$innsendingId: Getting the following files from Soknadsfillager: $ids")

		return filesApi.findFilesByIds(ids,false,innsendingId)
			.map { FilElementDto(it.id, it.content, it.createdAt) }
	}

	override fun delete(innsendingId: String, ids: List<String>) {
		logger.info("$innsendingId: Deleting the following files from Soknadsfillager: $ids")

		filesApi.deleteFiles(ids, innsendingId)
	}


}
