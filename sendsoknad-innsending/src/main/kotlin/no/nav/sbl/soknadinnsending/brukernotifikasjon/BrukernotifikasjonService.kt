package no.nav.sbl.soknadinnsending.brukernotifikasjon

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.soknad.arkivering.soknadsmottaker.api.CancelNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.api.NewNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.Serializer
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.time.OffsetDateTime

open class BrukernotifikasjonService(
	@Value("\${innsending.soknadsmottaker.host}") host: String,
	@Value("\${innsending.soknadsmottaker.username}") username: String,
	@Value("\${innsending.soknadsmottaker.password}") password: String,
	@Value("\${innsending.brukernotifikasjon.host}") private val brukernotifikasjonIngress: String
) : Brukernotifikasjon {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val newNotificationApi: NewNotificationApi
	private val cancelNotificationApi: CancelNotificationApi

	private val antalAktiveDager = 56
	private val tittelPrefixNySoknad = "Du har påbegynt en søknad om - "
	private val tittelPrefixNyEttersending = "Du har påbegynt en ettersending til - "
	private val linkSoknader = "/soknadinnsending/soknad/"
	private val linkSoknaderEttersending = "/soknadinnsending/startettersending/"


	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = username
		ApiClient.password = password
		newNotificationApi = NewNotificationApi(host)
		cancelNotificationApi = CancelNotificationApi(host)

		logger.info("Config for Soknadsmottaker (Tilbakemeldinger). Username: $username, password: ${password[0]}, host: $host")
	}


	override fun newNotification(
		skjemanavn: String,
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String
	) {
		val tittel = (if (erEttersendelse) tittelPrefixNyEttersending else tittelPrefixNySoknad) + skjemanavn
		val lenke = createLink(behandlingsId, erEttersendelse)

		newNotificationApi.newNotification(AddNotification(
			SoknadRef(behandlingsId, erEttersendelse, behandlingskjedeId, personId, OffsetDateTime.now()),
			NotificationInfo(tittel, lenke, antalAktiveDager, emptyList())
		))
	}

	override fun cancelNotification(
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String,
	) {
		cancelNotificationApi.cancelNotification(
			SoknadRef(behandlingsId, erEttersendelse, behandlingskjedeId, personId, OffsetDateTime.now())
		)
	}

	private fun createLink(behandlingsId: String, erEttersendelse: Boolean) =
		if (erEttersendelse)
			brukernotifikasjonIngress + linkSoknaderEttersending + behandlingsId
		else
			brukernotifikasjonIngress + linkSoknader + behandlingsId
}
