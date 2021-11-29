package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.SoknadInnsendtDto

interface Innsending {
	fun sendInn(data: SoknadInnsendtDto)
}
