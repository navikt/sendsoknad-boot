package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.*
import org.apache.commons.lang3.StringUtils
import java.time.LocalDateTime
import java.util.*

fun createSoknadInnsendtDto(soknad: Soknadsdata, vedleggsdata: List<Vedleggsdata>, hovedskjemas: Hovedskjemas): SoknadInnsendtDto {

	val hoveddokument = lagInnsendtDokumentForHovedskjema(soknad.skjemanummer, soknad.tittel, hovedskjemas)

	val dokument = listOf(hoveddokument).plus(
		vedleggsdata.map { lagInnsendtDokumentForVedlegg(it) }
	).toTypedArray()

	return SoknadInnsendtDto(
		UUID.randomUUID().toString(), soknad.erEttersending, soknad.aktoerId, soknad.tema, LocalDateTime.now(), dokument
	)
}

private fun lagInnsendtDokumentForHovedskjema(skjemanummer: String, tittel: String, hovedskjemas: Hovedskjemas) =
	InnsendtDokumentDto(
		skjemanummer, true, tittel, createInnsendtVariantDto(skjemanummer, hovedskjemas)
	)

private fun lagInnsendtDokumentForVedlegg(vedleggsdata: Vedleggsdata) =
	InnsendtDokumentDto(vedleggsdata.skjemanummer, false, vedleggsdata.tittel, toInnsendtVariantDto(vedleggsdata))


private fun createInnsendtVariantDto(skjemanummer: String, hovedskjemas: Hovedskjemas): Array<InnsendtVariantDto> {

	fun createInnsendtVariantDto(data: Hovedskjemadata, variantformat: String) =
		InnsendtVariantDto(
			data.id, "$skjemanummer.${data.fileType.lowercase()}", data.fileSize, variantformat, data.fileType.uppercase()
		)

	val output: MutableList<InnsendtVariantDto> = ArrayList()

	output.add(createInnsendtVariantDto(hovedskjemas.arkiv, "ARKIV"))
	if (hovedskjemas.fullversjon != null)
		output.add(createInnsendtVariantDto(hovedskjemas.fullversjon, "FULLVERSJON"))
	if (hovedskjemas.original != null)
		output.add(createInnsendtVariantDto(hovedskjemas.original, "ORIGINAL"))

	return output.toTypedArray()
}

private fun toInnsendtVariantDto(vedleggsdata: Vedleggsdata): Array<InnsendtVariantDto> {
	val mimetype = if (StringUtils.isEmpty(vedleggsdata.mimetype)) "application/pdf" else vedleggsdata.mimetype!!
	val filename = if (StringUtils.isEmpty(vedleggsdata.filename)) vedleggsdata.skjemanummer else vedleggsdata.filename!!

	return arrayOf(
		InnsendtVariantDto(vedleggsdata.id, filename, vedleggsdata.fileSize, "ARKIV",
			finnFiltype(mimetype, filename)
		)
	)
}

private fun finnFiltype(mimetype: String, filename: String): String {
	if (mimetype.contains("application/pdf")) {
		return if (filename.contains(".pdfa")) {
			"PDF/A"
		} else {
			"PDF"
		}
	} else if (mimetype.contains("application/")) {
		return mimetype.substring("application/".length).uppercase(Locale.getDefault())
	}
	return "UKJENT"
}
