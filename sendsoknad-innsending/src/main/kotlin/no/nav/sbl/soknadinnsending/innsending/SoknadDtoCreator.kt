package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import no.nav.soknad.arkivering.soknadsmottaker.model.DocumentData
import no.nav.soknad.arkivering.soknadsmottaker.model.Soknad
import no.nav.soknad.arkivering.soknadsmottaker.model.Varianter

fun createSoknad(
	soknadsdata: Soknadsdata,
	vedleggsdata: Collection<Vedleggsdata>,
	hovedskjemas: Collection<Hovedskjemadata>
): Soknad {

	val hoveddokument = lagInnsendtDokumentForHovedskjema(soknadsdata.skjemanummer, soknadsdata.tittel, hovedskjemas)

	val dokument = listOf(hoveddokument).plus(
		vedleggsdata.map { lagInnsendtDokumentForVedlegg(it) }
	)

	return Soknad(soknadsdata.behandlingId, soknadsdata.erEttersending, soknadsdata.aktoerId, soknadsdata.tema, dokument)
}

private fun lagInnsendtDokumentForHovedskjema(skjemanummer: String, tittel: String, hovedskjemas: Collection<Hovedskjemadata>) =
	DocumentData(
		skjemanummer, true, tittel, createInnsendtVariantDto(hovedskjemas)
	)

private fun lagInnsendtDokumentForVedlegg(vedleggsdata: Vedleggsdata) =
	DocumentData(vedleggsdata.skjemanummer, false, vedleggsdata.tittel, toInnsendtVariantDto(vedleggsdata))


private fun createInnsendtVariantDto(hovedskjemas: Collection<Hovedskjemadata>): List<Varianter> {

	return hovedskjemas.map {
		Varianter(
			it.id, it.mediatype, it.fileName, it.fileType.uppercase()
		)
	}
}

private fun toInnsendtVariantDto(vedleggsdata: Vedleggsdata): List<Varianter> =
	listOf(Varianter(vedleggsdata.id, vedleggsdata.mediatype, vedleggsdata.filename, vedleggsdata.fileType))
