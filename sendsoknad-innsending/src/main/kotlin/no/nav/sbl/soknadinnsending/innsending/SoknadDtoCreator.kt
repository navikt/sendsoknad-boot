package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter
import org.apache.commons.lang3.StringUtils
import java.util.*

fun createSoknad(
	soknadsdata: Soknadsdata,
	vedleggsdata: Collection<Vedleggsdata>,
	hovedskjemas: Collection<Hovedskjemadata>
): Soknad {

	val hoveddokument = lagInnsendtDokumentForHovedskjema(soknadsdata.skjemanummer, soknadsdata.tittel, hovedskjemas)

	val dokument = listOf(hoveddokument).plus(
		vedleggsdata.map { lagInnsendtDokumentForVedlegg(it) }
	)

	return Soknad(UUID.randomUUID().toString(), soknadsdata.erEttersending, soknadsdata.aktoerId, soknadsdata.tema, dokument)
}

private fun lagInnsendtDokumentForHovedskjema(skjemanummer: String, tittel: String, hovedskjemas: Collection<Hovedskjemadata>) =
	DocumentData(
		skjemanummer, true, tittel, createInnsendtVariantDto(skjemanummer, hovedskjemas)
	)

private fun lagInnsendtDokumentForVedlegg(vedleggsdata: Vedleggsdata) =
	DocumentData(vedleggsdata.skjemanummer, false, vedleggsdata.tittel, toInnsendtVariantDto(vedleggsdata))


private fun createInnsendtVariantDto(skjemanummer: String, hovedskjemas: Collection<Hovedskjemadata>): List<Varianter> {
	fun getFileExtension(fileType: String) = fileType.replace("[^a-zA-Z]".toRegex(), "").lowercase()

	return hovedskjemas.map {
		Varianter(
			it.id, it.mediatype, "$skjemanummer.${getFileExtension(it.fileType)}", it.fileType.uppercase()
		)
	}
}

private fun toInnsendtVariantDto(vedleggsdata: Vedleggsdata): List<Varianter> {
	val mediatype = if (StringUtils.isEmpty(vedleggsdata.mediatype)) "application/pdf" else vedleggsdata.mediatype!!
	val filename  = if (StringUtils.isEmpty(vedleggsdata.filename)) vedleggsdata.skjemanummer else vedleggsdata.filename!!

	return listOf(Varianter(vedleggsdata.id, mediatype, filename, finnFiltype(mediatype, filename)))
}

private fun finnFiltype(mediatype: String, filename: String): String {
	if (mediatype.contains("application/pdf")) {
		return if (filename.contains(".pdfa")) {
			"PDF/A"
		} else {
			"PDF"
		}
	} else if (mediatype.contains("application/")) {
		return mediatype.substring("application/".length).uppercase(Locale.getDefault())
	}
	return "UKJENT"
}
