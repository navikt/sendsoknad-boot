package no.nav.sbl.soknadinnsending.fillager.dto

import java.time.OffsetDateTime

data class FilElementDto(val id: String, val content: ByteArray?, val createdAt: OffsetDateTime?) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as FilElementDto

		if (id != other.id) return false
		if (!content.contentEquals(other.content)) return false
		if (createdAt != other.createdAt) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + content.contentHashCode()
		result = 31 * result + createdAt.hashCode()
		return result
	}
}
