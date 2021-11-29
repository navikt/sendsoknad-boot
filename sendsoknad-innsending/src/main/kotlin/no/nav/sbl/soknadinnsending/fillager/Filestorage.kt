package no.nav.sbl.soknadinnsending.fillager

import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto

interface Filestorage {
	fun store(files: List<FilElementDto>)
	fun get(ids: List<String>): List<FilElementDto>
	fun delete(ids: List<String>)
}
