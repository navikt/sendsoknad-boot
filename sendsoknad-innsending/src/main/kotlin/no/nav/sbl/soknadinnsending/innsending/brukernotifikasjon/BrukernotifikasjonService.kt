package no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.sbl.soknadinnsending.innsending.InnsendingConfig
import no.nav.soknad.arkivering.soknadsmottaker.api.CancelNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.api.NewNotificationApi
import no.nav.soknad.arkivering.soknadsmottaker.api.SoknadApi
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.ApiClient
import no.nav.soknad.arkivering.soknadsmottaker.infrastructure.Serializer
import no.nav.soknad.arkivering.soknadsmottaker.model.AddNotification
import no.nav.soknad.arkivering.soknadsmottaker.model.NotificationInfo
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Configuration
open class ApiClientConfig {
	@Bean
	open fun newNotificationClient(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		soknadsMottakerHttpClient: OkHttpClient
	): NewNotificationApi = NewNotificationApi(host, soknadsMottakerHttpClient)

	@Bean
	open fun cancelNotificationClient(
		@Value("\${innsending.soknadsmottaker.host}") host: String,
		soknadsMottakerHttpClient: OkHttpClient
	): CancelNotificationApi = CancelNotificationApi(host, soknadsMottakerHttpClient)
}

@Service
open class BrukernotifikasjonService(
	val newNotificationApi: NewNotificationApi,
	val cancelNotificationApi: CancelNotificationApi,
	@Value("\${innsending.brukernotifikasjon.host}") private val brukernotifikasjonIngress: String
) : Brukernotifikasjon {
	private val logger = LoggerFactory.getLogger(javaClass)


	private val antalAktiveDager = 56
	private val tittelPrefixNySoknad = "Du har påbegynt en søknad om - "
	private val tittelPrefixNyEttersending = "Du har påbegynt en ettersending til - "
	private val linkSoknader = "/soknadinnsending/soknad/"
	private val linkSoknaderEttersending = "/soknadinnsending/startettersending/"



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
