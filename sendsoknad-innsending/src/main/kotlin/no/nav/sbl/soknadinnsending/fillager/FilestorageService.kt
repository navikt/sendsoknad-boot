package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto
import no.nav.soknad.arkivering.soknadsfillager.api.FilesApi
import no.nav.soknad.arkivering.soknadsfillager.model.FileData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FilestorageService(private val filesClient: FilesApi) : Filestorage {
	private val logger = LoggerFactory.getLogger(javaClass)


	override fun store(innsendingId: String, files: List<FilElementDto>) {
		logger.info("$innsendingId: Storing the following files in Soknadsfillager: ${files.map { it.id }}")
		val filedata = files.map { FileData(it.id, it.content, it.createdAt) }

		filesClient.addFiles(filedata, innsendingId, "enabled")
	}

	override fun getFileMetadata(innsendingId: String, ids: List<String>): List<FileData> {
		logger.info("$innsendingId: Getting metadata for the following files from Soknadsfillager: $ids")
		return filesClient.findFilesByIds(ids, true, innsendingId)
	}

	override fun getFiles(innsendingId: String, ids: List<String>): List<FilElementDto> {
		logger.info("$innsendingId: Getting the following files from Soknadsfillager: $ids")

		return filesClient.findFilesByIds(ids, false, innsendingId)
			.map { FilElementDto(it.id, it.content, it.createdAt) }
	}

	override fun delete(innsendingId: String, ids: List<String>) {
		logger.info("$innsendingId: Deleting the following files from Soknadsfillager: $ids")

		filesClient.deleteFiles(ids, innsendingId)
	}
}
