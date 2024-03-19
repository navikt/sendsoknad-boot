package no.nav.sbl.dialogarena.kodeverk

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import no.nav.sbl.dialogarena.kodeverk.config.OkHttpClientConfig
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Profile

@Profile("local")
class KodeverkServiceTest {

    val client = OkHttpClientConfig()
    val kodeverkService: KodeverkService = KodeverkService(kodeverkApiClient = client.kodeverkApiClient())

    @Test
    fun sjekkHentLandKoder() {
        val landkoder = kodeverkService.hentAlleKodenavnFraKodeverk(KodeverkInterface.KodeverkType.LANDKODE)

        assertTrue(landkoder != null && landkoder.isNotEmpty())
        assertTrue(landkoder.contains("NOR"))
        assertTrue(landkoder.indexOf("ALA") > landkoder.indexOf("NOR"))
    }

    @Test
    fun hentKommuneKoder() {
        val kommuneKoder = kodeverkService.hentAlleKodenavnFraKodeverk(KodeverkInterface.KodeverkType.KOMMUNE)
        assertTrue(kommuneKoder != null && kommuneKoder.isNotEmpty())
        assertTrue(kommuneKoder.contains("4645"))
    }

    @Test

    fun hentPostnummere() {
        val postnummere = kodeverkService.hentAlleKodenavnFraKodeverk(KodeverkInterface.KodeverkType.POSTNUMMER)
        assertTrue(postnummere != null && postnummere.isNotEmpty())
        assertTrue(postnummere.contains("0682") && postnummere.contains("3701"))
        assertTrue(postnummere.indexOf("3701") > postnummere.indexOf("0682"))

    }

    @Test
    fun hentPoststedForPostnummer() {
        val poststed = kodeverkService.getPoststed("0682")
        assertEquals("OSLO", poststed)
    }

    @Test
    fun hentLandForLandkode() {
        val land = kodeverkService.getLand("NOR")
        assertEquals("Norge", land)
    }

    @Test
    fun hentIkkeEksisterendeLandGirNull() {
        Assert.assertNull(kodeverkService.getLand("YUG"))
    }


}