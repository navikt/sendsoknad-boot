package no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon

interface Brukernotifikasjon {
	fun newNotification(
		skjemanavn: String,
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String
	)

	fun cancelNotification(
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String
	)
}