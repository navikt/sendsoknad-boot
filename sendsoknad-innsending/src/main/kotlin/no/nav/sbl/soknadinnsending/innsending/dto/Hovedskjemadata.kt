package no.nav.sbl.soknadinnsending.innsending.dto

data class Hovedskjemadata(val id: String, val fileSize: Long, val fileType: String, val type: HovedskjemaType)

enum class HovedskjemaType{ ARKIV, FULLVERSJON, ORIGINAL }
