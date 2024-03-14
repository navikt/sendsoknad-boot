package no.nav.sbl.dialogarena.kodeverk

interface KodeverkInterface {
    enum class KodeverkType(val value: String) {
        LANDKODE("Landkoder"),
        KOMMUNE("Kommuner"),
        POSTNUMMER("Postnummer"),
    }

    fun getLand(landkode: String?): String?

    fun getPoststed(postnummer: String?): String?

    fun lastInnNyeKodeverk()

    fun hentAlleKodenavnFraKodeverk(kodeverknavn: KodeverkType): List<String>?

}