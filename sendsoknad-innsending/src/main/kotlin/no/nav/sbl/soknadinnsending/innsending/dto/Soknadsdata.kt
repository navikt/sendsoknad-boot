package no.nav.sbl.soknadinnsending.innsending.dto

data class Soknadsdata(
	val skjemanummer: String,
	val erEttersending: Boolean,
	val aktoerId: String,
	val tema: String,
	val tittel: String
)
