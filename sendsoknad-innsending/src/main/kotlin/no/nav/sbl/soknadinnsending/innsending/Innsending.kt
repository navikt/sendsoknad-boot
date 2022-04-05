package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata

interface Innsending {
	fun sendInn(soknadsdata: Soknadsdata, vedleggsdata: Collection<Vedleggsdata>, hovedskjemas: Collection<Hovedskjemadata>)
}
