package no.nav.sbl.soknadinnsending.innsending.dto

import java.time.LocalDateTime

data class SoknadInnsendtDto(val innsendingsId: String, val ettersendelse: Boolean, val personId: String, val tema: String,
                             val innsendtDato: LocalDateTime, val innsendteDokumenter: Array<InnsendtDokumentDto>)

data class InnsendtDokumentDto(val skjemaNummer: String, val erHovedSkjema: Boolean,
                               val tittel: String, val varianter: Array<InnsendtVariantDto>)

data class InnsendtVariantDto(val id: String, val filNavn: String, val filStorrelse: Long, val variantformat: String,
															val filtype: String)
