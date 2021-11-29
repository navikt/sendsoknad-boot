package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemas
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SoknadInnsendtDtoCreatorTest {

	@Test
	fun `Can create SoknadInnsendtDto - no Vedlegg, one hovedskjemavariant`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = Hovedskjemas(Hovedskjemadata("68", 63L, "PDF"), null, null)

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, emptyList(), hovedskjemas)

		assertTrue(soknadInnsendtDto.innsendtDato.isBefore(LocalDateTime.now().plusNanos(1)))
		assertEquals(soknadsdata.erEttersending, soknadInnsendtDto.ettersendelse)
		assertEquals(soknadsdata.tema, soknadInnsendtDto.tema)
		assertEquals(soknadsdata.aktoerId, soknadInnsendtDto.personId)

		assertEquals(1, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(soknadsdata.skjemanummer, soknadInnsendtDto.innsendteDokumenter[0].skjemaNummer)
		assertEquals(soknadsdata.tittel, soknadInnsendtDto.innsendteDokumenter[0].tittel)
		assertTrue(soknadInnsendtDto.innsendteDokumenter[0].erHovedSkjema)

		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[0].varianter.size)
		assertEquals(hovedskjemas.arkiv.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas.arkiv.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filStorrelse)
		assertEquals(hovedskjemas.arkiv.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas.arkiv.fileType.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filNavn)
		assertEquals("ARKIV", soknadInnsendtDto.innsendteDokumenter[0].varianter[0].variantformat)
	}

	@Test
	fun `Can create SoknadInnsendtDto - no Vedlegg, three hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = Hovedskjemas(
			Hovedskjemadata("68", 63L, "PDFA"),
			Hovedskjemadata("75", 76L, "PDF"),
			Hovedskjemadata("90", 78L, "JSON"))

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, emptyList(), hovedskjemas)

		assertEquals(1, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(3, soknadInnsendtDto.innsendteDokumenter[0].varianter.size)

		assertEquals(hovedskjemas.arkiv.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas.arkiv.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filStorrelse)
		assertEquals(hovedskjemas.arkiv.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas.arkiv.fileType.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filNavn)
		assertEquals("ARKIV", soknadInnsendtDto.innsendteDokumenter[0].varianter[0].variantformat)

		assertEquals(hovedskjemas.fullversjon?.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].id)
		assertEquals(hovedskjemas.fullversjon?.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filStorrelse)
		assertEquals(hovedskjemas.fullversjon?.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filtype)
		val expectedFilenamefullversjon = "${soknadsdata.skjemanummer}.${hovedskjemas.fullversjon?.fileType?.lowercase()}"
		assertEquals(expectedFilenamefullversjon, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filNavn)
		assertEquals("FULLVERSJON", soknadInnsendtDto.innsendteDokumenter[0].varianter[1].variantformat)

		assertEquals(hovedskjemas.original?.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].id)
		assertEquals(hovedskjemas.original?.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filStorrelse)
		assertEquals(hovedskjemas.original?.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filtype)
		val expectedFilenameOriginal = "${soknadsdata.skjemanummer}.${hovedskjemas.original?.fileType?.lowercase()}"
		assertEquals(expectedFilenameOriginal, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filNavn)
		assertEquals("ORIGINAL", soknadInnsendtDto.innsendteDokumenter[0].varianter[2].variantformat)
	}

	@Test
	fun `Can create SoknadInnsendtDto - one Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", 81, "name", "-")
		val hovedskjemas = Hovedskjemas(Hovedskjemadata("68", 63L, "PDF"), null, null)

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(vedlegg.skjemanummer, soknadInnsendtDto.innsendteDokumenter[1].skjemaNummer)
		assertEquals(vedlegg.tittel, soknadInnsendtDto.innsendteDokumenter[1].tittel)
		assertFalse(soknadInnsendtDto.innsendteDokumenter[1].erHovedSkjema)

		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[1].varianter.size)
		assertEquals(vedlegg.id, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].id)
		assertEquals(vedlegg.fileSize, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filStorrelse)
		assertEquals(vedlegg.filename, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filNavn) //TODO: file extension?
		assertEquals("UKJENT", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filtype)
		assertEquals("ARKIV", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].variantformat)
	}

	@Test
	fun `Can create SoknadInnsendtDto - one json Vedlegg with no name, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", 81, "", "application/json")
		val hovedskjemas = Hovedskjemas(Hovedskjemadata("68", 63L, "PDF"), null, null)

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[1].varianter.size)
		assertEquals(vedlegg.skjemanummer, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filNavn) //TODO: file extension?
		assertEquals("JSON", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filtype)
	}

	@Test
	fun `Can create SoknadInnsendtDto - one pdf Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", 81, "name.pdf", "application/pdf")
		val hovedskjemas = Hovedskjemas(Hovedskjemadata("68", 63L, "PDF"), null, null)

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[1].varianter.size)
		assertEquals(vedlegg.filename, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filNavn) //TODO: file extension?
		assertEquals("PDF", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filtype)
	}

	@Test
	fun `Can create SoknadInnsendtDto - one pdfa Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", 81, "name.pdfa", "application/pdf")
		val hovedskjemas = Hovedskjemas(Hovedskjemadata("68", 63L, "PDF"), null, null)

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[1].varianter.size)
		assertEquals(vedlegg.filename, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filNavn) //TODO: file extension?
		assertEquals("PDF/A", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filtype)
	}
}
