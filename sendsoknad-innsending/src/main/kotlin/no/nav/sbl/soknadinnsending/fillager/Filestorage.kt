package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto

interface Filestorage {
	fun store(innsendingId: String, files: List<FilElementDto>)
	fun get(innsendingId: String, ids: List<String>): List<FilElementDto>
	fun delete(innsendingId: String, ids: List<String>)
}
