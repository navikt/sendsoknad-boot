package no.nav.sbl.soknadinnsending.innsending.dto

data class Vedleggsdata(
	val id: String,
	val skjemanummer: String,
	val tittel: String,
	val fileSize: Long,
	val filename: String?,
	val mimetype: String?
)
