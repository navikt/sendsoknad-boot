package no.nav.sbl.dialogarena.kodeverk

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import no.nav.soknad.sendsoknad.kodeverk.api.KodeverkApi
import no.nav.soknad.sendsoknad.kodeverk.model.Betydning
import no.nav.soknad.sendsoknad.kodeverk.model.GetKodeverkKoderBetydningerResponse
import okhttp3.OkHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class KodeverkService(
    @Qualifier("kodeverkApiClient") kodeverkApiClient: OkHttpClient
): KodeverkInterface {

    @Value("\${sendsoknad.rest.kodeverk.url}")
    private val kodeverkEndPoint: String = "https://kodeverk-api.nav.no"

    private val sprak = "nb"

    val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val kodeverkApi = KodeverkApi(kodeverkEndPoint, kodeverkApiClient)

    val cache: LoadingCache<String, GetKodeverkKoderBetydningerResponse> = Caffeine
        .newBuilder()
        .refreshAfterWrite(Duration.ofHours(1))
        .build {
            kodeverkApi.betydning(
                kodeverksnavn = it,
                spraak = setOf("nb", "nn", "en"),
                ekskluderUgyldige = true
            )
        }

    private fun getKodeverk(kodeverkType: KodeverkInterface.KodeverkType): GetKodeverkKoderBetydningerResponse? {
        val response = try {
            cache.get(kodeverkType.value)
        } catch (e: Exception) {
            logger.error("Kunne ikke hente kodeverk ${kodeverkType.value}.", e)
        }
        if (response == null) return null
        if (response is GetKodeverkKoderBetydningerResponse) {
            return response
        } else {
            logger.warn("Fant ikke data for kodeverk $kodeverkType. Oppslag mot $kodeverkEndPoint")
            return null
        }
    }

    override fun getLand(landkode: String?): String? {
        if (landkode == null) return null
        val kodeverkLand = getKodeverk(KodeverkInterface.KodeverkType.LANDKODE)
        if (kodeverkLand == null) {
            logger.warn("Fant ikke land navn for kode $landkode. Klarte ikke å hente kodeverk for land.")
            return ""
        }
        val landnavnFraKodeverk = kodeverkLand.betydninger[landkode]?.first()?.beskrivelser?.get(sprak)?.term

        return formaterLand(landnavnFraKodeverk)
    }

    override fun getPoststed(postnummer: String?): String? {
        if (postnummer == null) return null

        val postnummere = getKodeverk(KodeverkInterface.KodeverkType.POSTNUMMER)
        if (postnummere == null) {
            logger.warn("Fant ikke postnummer navn for kode $postnummer. Klarte ikke å hente kodeverk for postnummer.")
            return ""
        }

        return postnummere.betydninger[postnummer]?.first()?.beskrivelser?.get(sprak)?.term
    }

    override fun lastInnNyeKodeverk() {
        KodeverkInterface.KodeverkType.entries.map { it }.forEach { getKodeverk(it) }
    }

    override fun hentAlleKodenavnFraKodeverk(kodeverknavn: KodeverkInterface.KodeverkType): List<String> {
        val kodeverk = getKodeverk(kodeverknavn)

        if (kodeverk == null) {
            logger.warn("Fant ikke kodeverket $kodeverknavn.")
            return emptyList()
        }

        if (kodeverknavn == KodeverkInterface.KodeverkType.POSTNUMMER) {
            return kodeverk.betydninger.map { it.key }.sorted()
        }

        // Skal returnere koder sortert på term
        return kodeverk.betydninger
            .filter{it.value.first().beskrivelser.get(sprak)?.term != null}
            .toList()
            .sortedBy { (_, value) -> value.first().beskrivelser.get(sprak)?.term }
            .toMap()
            .map{it.key}
    }

    private fun formaterLand(land: String?): String? {
        if (land == null) return null

        val formaterMedSpace = setUpperCaseBeforeRegex(land.lowercase(Locale.getDefault()), " ")
        val formaterMedDash = setUpperCaseBeforeRegex(formaterMedSpace, "-")
        return setUpperCaseBeforeRegex(formaterMedDash, "/")
    }

    private fun setUpperCaseBeforeRegex(s: String, regex: String): String {
        val split = s.split(regex.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()
        for (i in split.indices) {
            if (i > 0) {
                sb.append(regex)
            }

            if (split[i] == "og") {
                sb.append(split[i])
            } else {
                sb.append(split[i].substring(0, 1).uppercase(Locale.getDefault()))
                sb.append(split[i].substring(1))
            }
        }
        return sb.toString()
    }


}