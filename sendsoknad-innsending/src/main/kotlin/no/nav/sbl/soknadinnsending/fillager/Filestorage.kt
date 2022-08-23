package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto
import no.nav.soknad.arkivering.soknadsfillager.model.FileData

interface Filestorage {
	fun store(innsendingId: String, files: List<FilElementDto>)
	fun getFiles(innsendingId: String, ids: List<String>): List<FilElementDto>
	fun getFileMetadata(innsendingId: String, ids: List<String>) :List<FileData>
	fun delete(innsendingId: String, ids: List<String>)
	fun check(innsendingId: String, ids: List<String>): Boolean
}
