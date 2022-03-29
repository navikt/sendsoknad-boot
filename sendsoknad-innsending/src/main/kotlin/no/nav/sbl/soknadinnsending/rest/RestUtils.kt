package no.nav.sbl.soknadinnsending.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

private val logger = LoggerFactory.getLogger("no.nav.sbl.soknadinnsending.rest.RestUtils")

private val restClient = OkHttpClient()
val objectMapper = ObjectMapper().also {
	it.findAndRegisterModules()
	it.registerModule(JavaTimeModule())
}


fun performGetCall(url: String, usernameAndPassword: Pair<String, String>): ByteArray? {

	val headers = createHeaders(usernameAndPassword)
	val request = Request.Builder().url(url).headers(headers).get().build()

	restClient.newCall(request).execute().use {
		return if (it.isSuccessful) {
			it.body?.bytes()
		} else {
			logger.error("Get call not successful: ${it.networkResponse}")
			null
		}
	}
}

fun performPostCall(payload: Any, url: String, usernameAndPassword: Pair<String, String>, async: Boolean) =
	performPostCall(payload, url, listOf(usernameAndPassword), async)

fun performPostCall(payload: Any, url: String, headerPairs: List<Pair<String, String>>, async: Boolean) {
	val requestBody = object : RequestBody() {
		override fun contentType() = "application/json".toMediaType()
		override fun writeTo(sink: BufferedSink) {
			sink.writeUtf8(objectMapper.writeValueAsString(payload))
		}
	}

	val headers = createHeaders(headerPairs.first(), headerPairs.drop(1))
	val request = Request.Builder().url(url).headers(headers).post(requestBody).build()

	val call = restClient.newCall(request)
	if (async)
		call.enqueue(restRequestCallback)
	else {
		call.execute().close()
	}
}

fun performDeleteCall(url: String, usernameAndPassword: Pair<String, String>) {
	val requestBody = object : RequestBody() {
		override fun contentType() = "application/json".toMediaType()
		override fun writeTo(sink: BufferedSink) {}
	}

	val headers = createHeaders(usernameAndPassword)
	val request = Request.Builder().headers(headers).url(url).delete(requestBody).build()
	restClient.newCall(request).execute().close()
}

private val restRequestCallback = object : Callback {
	override fun onResponse(call: Call, response: Response) {}

	override fun onFailure(call: Call, e: IOException) {
		throw e
	}
}

private fun createHeaders(usernameAndPassword: Pair<String, String>) = createHeaders(usernameAndPassword, emptyList())

private fun createHeaders(usernameAndPassword: Pair<String, String>, headers: List<Pair<String, String>>): Headers {
	val auth = "${usernameAndPassword.first}:${usernameAndPassword.second}"
	val authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.toByteArray())

	val allHeaders = listOf(Pair("Authorization", authHeader)).plus(headers)
	val h = allHeaders.flatMap { listOf(it.first, it.second) }.toTypedArray()

	return Headers.headersOf(*h)
}
