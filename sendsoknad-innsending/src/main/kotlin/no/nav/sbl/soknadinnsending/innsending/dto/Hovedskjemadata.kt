package no.nav.sbl.soknadinnsending.innsending.dto

data class Hovedskjemas(
	val arkiv: Hovedskjemadata,
	val fullversjon: Hovedskjemadata?,
	val original: Hovedskjemadata?
)

data class Hovedskjemadata(val id: String, val fileSize: Long, val fileType: String)
