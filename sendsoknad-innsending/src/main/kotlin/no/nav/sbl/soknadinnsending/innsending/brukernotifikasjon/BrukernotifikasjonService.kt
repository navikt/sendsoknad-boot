package no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon

import no.nav.soknad.arkivering.soknadsmottaker.api.CancelNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.api.NewNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Configuration
open class ApiClientConfig {
	@Bean
	open fun newNotificationClient(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		soknadsmottakerHttpClient: OkHttpClient
	): NewNotificationApi = NewNotificationApi(host, soknadsmottakerHttpClient)

	@Bean
	open fun cancelNotificationClient(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		soknadsmottakerHttpClient: OkHttpClient
	): CancelNotificationApi = CancelNotificationApi(host, soknadsmottakerHttpClient)
}

@Service
open class BrukernotifikasjonService(
	private val newNotificationApi: NewNotificationApi,
	private val cancelNotificationApi: CancelNotificationApi,
	@Value("\${innsending.brukernotifikasjon.tjensteUrl}") private val tjensteUrl: String
) : Brukernotifikasjon {
	private val logger = LoggerFactory.getLogger(javaClass)

	private val antalAktiveDager = 56
	private val tittelPrefixNySoknad = "Du har påbegynt en søknad om - "
	private val tittelPrefixNyEttersending = "Du har påbegynt en ettersending til - "
	private val linkSoknader = "/soknadinnsending/soknad/"

	private val dryRun = null

	override fun newNotification(
		skjemanavn: String,
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String
	) {
		logger.info(behandlingsId+ ": Skal sende melding til soknadsmottaker for publisering av ny brukernotifikasjon")
		try {
			val tittel = (if (erEttersendelse) tittelPrefixNyEttersending else tittelPrefixNySoknad) + skjemanavn
			val lenke = createLink(behandlingsId, erEttersendelse)

			newNotificationApi.newNotification(
				AddNotification(
					SoknadRef(behandlingsId, erEttersendelse, behandlingskjedeId, personId, OffsetDateTime.now()),
					NotificationInfo(tittel, lenke, antalAktiveDager, emptyList())
				), dryRun
			)
		} catch (e: Exception) {
			logger.error("$behandlingsId: Failed to create New Brukernotifikasjon", e)
		}
	}

	override fun cancelNotification(
		behandlingsId: String,
		behandlingskjedeId: String,
		erEttersendelse: Boolean,
		personId: String,
	) {
		try {
			cancelNotificationApi.cancelNotification(
				SoknadRef(behandlingsId, erEttersendelse, behandlingskjedeId, personId, OffsetDateTime.now()), dryRun
			)
		} catch (e: Exception) {
			logger.error("$behandlingsId: Failed to Cancel Brukernotifikasjon", e)
		}
	}

	private fun createLink(behandlingsId: String, erEttersendelse: Boolean = false) = tjensteUrl + (if (erEttersendelse)  "/soknadinnsending/startettersending/" else  linkSoknader)  +  behandlingsId
}
