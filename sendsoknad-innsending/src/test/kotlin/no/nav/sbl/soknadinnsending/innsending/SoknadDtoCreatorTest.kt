package no.nav.sbl.soknadinnsending.innsending

import no.nav.sbl.soknadinnsending.innsending.dto.Hovedskjemadata
import no.nav.sbl.soknadinnsending.innsending.dto.Soknadsdata
import no.nav.sbl.soknadinnsending.innsending.dto.Vedleggsdata
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SoknadDtoCreatorTest {

	@Test
	fun `Can create Soknad - no Vedlegg, one hovedskjemavariant`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", "PDF"))

		val soknad = createSoknad(soknadsdata, emptyList(), hovedskjemas)

		assertEquals(soknadsdata.erEttersending, soknad.erEttersendelse)
		assertEquals(soknadsdata.tema, soknad.tema)
		assertEquals(soknadsdata.aktoerId, soknad.personId)

		assertEquals(1, soknad.dokumenter.size)
		assertEquals(soknadsdata.skjemanummer, soknad.dokumenter[0].skjemanummer)
		assertEquals(soknadsdata.tittel, soknad.dokumenter[0].tittel)
		assertTrue(soknad.dokumenter[0].erHovedskjema)

		assertEquals(1, soknad.dokumenter[0].varianter.size)
		assertEquals(hovedskjemas[0].id, soknad.dokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas[0].fileType, soknad.dokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas[0].fileType.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknad.dokumenter[0].varianter[0].filnavn)
		assertEquals("application/pdf", soknad.dokumenter[0].varianter[0].mediaType)
	}

	@Test
	fun `Can create Soknad - no Vedlegg, three hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val hovedskjemas = mapOf(
			"ARKIV" to Hovedskjemadata("68", "application/pdfa", "PDFA"),
			"FULLVERSJON" to Hovedskjemadata("75", "application/pdf", "PDF"),
			"ORIGINAL" to Hovedskjemadata("90", "application/json", "JSON"))

		val soknad = createSoknad(soknadsdata, emptyList(), hovedskjemas.values)

		assertEquals(1, soknad.dokumenter.size)
		assertEquals(3, soknad.dokumenter[0].varianter.size)

		assertEquals(hovedskjemas["ARKIV"]?.id, soknad.dokumenter[0].varianter[0].id)
		assertEquals(hovedskjemas["ARKIV"]?.fileType, soknad.dokumenter[0].varianter[0].filtype)
		val expectedFilenameArkiv = "${soknadsdata.skjemanummer}.${hovedskjemas["ARKIV"]?.fileType?.lowercase()}"
		assertEquals(expectedFilenameArkiv, soknad.dokumenter[0].varianter[0].filnavn)
		assertEquals("application/pdfa", soknad.dokumenter[0].varianter[0].mediaType)

		assertEquals(hovedskjemas["FULLVERSJON"]?.id, soknad.dokumenter[0].varianter[1].id)
		assertEquals(hovedskjemas["FULLVERSJON"]?.fileType, soknad.dokumenter[0].varianter[1].filtype)
		val expectedFilenamefullversjon = "${soknadsdata.skjemanummer}.${hovedskjemas["FULLVERSJON"]?.fileType?.lowercase()}"
		assertEquals(expectedFilenamefullversjon, soknad.dokumenter[0].varianter[1].filnavn)
		assertEquals("application/pdf", soknad.dokumenter[0].varianter[1].mediaType)

		assertEquals(hovedskjemas["ORIGINAL"]?.id, soknad.dokumenter[0].varianter[2].id)
		assertEquals(hovedskjemas["ORIGINAL"]?.fileType, soknad.dokumenter[0].varianter[2].filtype)
		val expectedFilenameOriginal = "${soknadsdata.skjemanummer}.${hovedskjemas["ORIGINAL"]?.fileType?.lowercase()}"
		assertEquals(expectedFilenameOriginal, soknad.dokumenter[0].varianter[2].filnavn)
		assertEquals("application/json", soknad.dokumenter[0].varianter[2].mediaType)
	}

	@Test
	fun `Can create Soknad - one Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", "name", "UNKNOWN")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", "PDF"))

		val soknad = createSoknad(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknad.dokumenter.size)
		assertEquals(vedlegg.skjemanummer, soknad.dokumenter[1].skjemanummer)
		assertEquals(vedlegg.tittel, soknad.dokumenter[1].tittel)
		assertFalse(soknad.dokumenter[1].erHovedskjema)

		assertEquals(1, soknad.dokumenter[1].varianter.size)
		assertEquals(vedlegg.id, soknad.dokumenter[1].varianter[0].id)
		assertEquals(vedlegg.filename, soknad.dokumenter[1].varianter[0].filnavn)
		assertEquals("UKJENT", soknad.dokumenter[1].varianter[0].filtype)
		assertEquals("UNKNOWN", soknad.dokumenter[1].varianter[0].mediaType)
	}

	@Test
	fun `Can create Soknad - one json Vedlegg with no name, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", "", "application/json")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", "PDF"))

		val soknad = createSoknad(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknad.dokumenter.size)
		assertEquals(1, soknad.dokumenter[1].varianter.size)
		assertEquals(vedlegg.skjemanummer, soknad.dokumenter[1].varianter[0].filnavn)
		assertEquals("JSON", soknad.dokumenter[1].varianter[0].filtype)
	}

	@Test
	fun `Can create Soknad - one pdf Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", "name.pdf", "application/pdf")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", "PDF"))

		val soknad = createSoknad(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknad.dokumenter.size)
		assertEquals(1, soknad.dokumenter[1].varianter.size)
		assertEquals(vedlegg.filename, soknad.dokumenter[1].varianter[0].filnavn)
		assertEquals("PDF", soknad.dokumenter[1].varianter[0].filtype)
	}

	@Test
	fun `Can create Soknad - one pdfa Vedlegg, one hovedskjemavarianter`() {

		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", "name.pdfa", "application/pdf")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", "PDF"))

		val soknad = createSoknad(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals(2, soknad.dokumenter.size)
		assertEquals(1, soknad.dokumenter[1].varianter.size)
		assertEquals(vedlegg.filename, soknad.dokumenter[1].varianter[0].filnavn)
		assertEquals("PDF/A", soknad.dokumenter[1].varianter[0].filtype)
	}

	@Test
	fun `When fileType contains non-standard chars, filename is still correct`() {
		val fileType = "Pö * D3F/A"
		val soknadsdata = Soknadsdata(UUID.randomUUID().toString(), "NAV 11-12.12", false, "71", "TSO", "title")
		val vedlegg = Vedleggsdata("78", "L7", "vedleggtitle", "name.pdfa", "application/pdf")
		val hovedskjemas = listOf(Hovedskjemadata("68", "application/pdf", fileType))

		val soknad = createSoknad(soknadsdata, listOf(vedlegg), hovedskjemas)

		assertEquals("NAV 11-12.12.pdfa", soknad.dokumenter[0].varianter[0].filnavn)
	}
}
