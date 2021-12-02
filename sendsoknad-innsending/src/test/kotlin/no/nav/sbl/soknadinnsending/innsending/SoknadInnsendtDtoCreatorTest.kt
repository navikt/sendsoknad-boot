package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.*
import no.nav.sbl.soknadinnsending.innsending.dto.HovedskjemaType.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SoknadInnsendtDtoCreatorTest {

	@Test
	fun `Can create SoknadInnsendtDto - no Vedlegg, one hovedskjemavariant`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = listOf(Hovedskjemadata("68", 63L, "PDF", ARKIV))

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
		assertEquals(hovedskjemas[0].id, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas[0].fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filStorrelse)
		assertEquals(hovedskjemas[0].fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas[0].fileType.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filNavn)
		assertEquals("ARKIV", soknadInnsendtDto.innsendteDokumenter[0].varianter[0].variantformat)
	}

	@Test
	fun `Can create SoknadInnsendtDto - no Vedlegg, three hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = mapOf(
			ARKIV to Hovedskjemadata("68", 63L, "PDFA", ARKIV),
			FULLVERSJON to Hovedskjemadata("75", 76L, "PDF", FULLVERSJON),
			ORIGINAL to Hovedskjemadata("90", 78L, "JSON", ORIGINAL))

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, emptyList(), hovedskjemas.values)

		assertEquals(1, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(3, soknadInnsendtDto.innsendteDokumenter[0].varianter.size)

		assertEquals(hovedskjemas[ARKIV]?.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas[ARKIV]?.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filStorrelse)
		assertEquals(hovedskjemas[ARKIV]?.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas[ARKIV]?.fileType?.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknadInnsendtDto.innsendteDokumenter[0].varianter[0].filNavn)
		assertEquals("ARKIV", soknadInnsendtDto.innsendteDokumenter[0].varianter[0].variantformat)

		assertEquals(hovedskjemas[FULLVERSJON]?.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].id)
		assertEquals(hovedskjemas[FULLVERSJON]?.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filStorrelse)
		assertEquals(hovedskjemas[FULLVERSJON]?.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filtype)
		val expectedFilenamefullversjon = "${soknadsdata.skjemanummer}.${hovedskjemas[FULLVERSJON]?.fileType?.lowercase()}"
		assertEquals(expectedFilenamefullversjon, soknadInnsendtDto.innsendteDokumenter[0].varianter[1].filNavn)
		assertEquals("FULLVERSJON", soknadInnsendtDto.innsendteDokumenter[0].varianter[1].variantformat)

		assertEquals(hovedskjemas[ORIGINAL]?.id, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].id)
		assertEquals(hovedskjemas[ORIGINAL]?.fileSize, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filStorrelse)
		assertEquals(hovedskjemas[ORIGINAL]?.fileType, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filtype)
		val expectedFilenameOriginal = "${soknadsdata.skjemanummer}.${hovedskjemas[ORIGINAL]?.fileType?.lowercase()}"
		assertEquals(expectedFilenameOriginal, soknadInnsendtDto.innsendteDokumenter[0].varianter[2].filNavn)
		assertEquals("ORIGINAL", soknadInnsendtDto.innsendteDokumenter[0].varianter[2].variantformat)
	}

	@Test
	fun `Can create SoknadInnsendtDto - one Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata("NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", 81, "name", "-")
		val hovedskjemas = listOf(Hovedskjemadata("68", 63L, "PDF", ARKIV))

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
		val hovedskjemas = listOf(Hovedskjemadata("68", 63L, "PDF", ARKIV))

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
		val hovedskjemas = listOf(Hovedskjemadata("68", 63L, "PDF", ARKIV))

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
		val hovedskjemas = listOf(Hovedskjemadata("68", 63L, "PDF", ARKIV))

		val soknadInnsendtDto = createSoknadInnsendtDto(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknadInnsendtDto.innsendteDokumenter.size)
		assertEquals(1, soknadInnsendtDto.innsendteDokumenter[1].varianter.size)
		assertEquals(vedlegg.filename, soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filNavn) //TODO: file extension?
		assertEquals("PDF/A", soknadInnsendtDto.innsendteDokumenter[1].varianter[0].filtype)
	}
}
