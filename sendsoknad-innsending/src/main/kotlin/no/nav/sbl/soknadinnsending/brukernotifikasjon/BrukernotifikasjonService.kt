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

class BrukernotifikasjonService : Brukernotifikasjon {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val newNotificationApi: NewNotificationApi
	private val cancelNotificationApi: CancelNotificationApi

	@Value("\${innsending.soknadsmottaker.host:localhost:8090}")
	private lateinit var soknadsmottakerHost: String
	@Value("\${INNSENDING_USERNAME}")
	private lateinit var soknadsmottakerUsername: String
	@Value("\${INNSENDING_PASSWORD}")
	private lateinit var soknadsmottakerPassword: String
	@Value("\${innsending.brukernotifikasjon.host:localhost}")
	private lateinit var brukernotifikasjonIngress: String

	private val antalAktiveDager = 56
	private val tittelPrefixNySoknad = "Du har påbegynt en søknad om - "
	private val tittelPrefixNyEttersending = "Du har påbegynt en ettersending til - "
	private val linkSoknader = "/soknadinnsending/soknad/"
	private val linkSoknaderEttersending = "/soknadinnsending/startettersending/"


	init {
		Serializer.jacksonObjectMapper.registerModule(JavaTimeModule())
		ApiClient.username = soknadsmottakerUsername
		ApiClient.password = soknadsmottakerPassword
		newNotificationApi = NewNotificationApi(soknadsmottakerHost)
		cancelNotificationApi = CancelNotificationApi(soknadsmottakerHost)

		logger.info("Config for Soknadsmottaker (Tilbakemeldinger). " +
			"Username: $soknadsmottakerUsername, password: ${soknadsmottakerPassword[0]}, host: $soknadsmottakerHost")
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
