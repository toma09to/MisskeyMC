package com.toma09to.misskeymc.misskey

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class MisskeyClient(fqdn: String, private val channelId: String?, private val token: String) {
    private val url = "https://$fqdn/"
    private val client = HttpClient(Java)

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private suspend inline fun <reified T> sendRequest(endPoint: String, body: T): HttpResponse {
        return client.post(url + endPoint) {
            headers {
                append("Authorization", "Bearer $token")
                append("Content-Type", "application/json")
            }
            setBody(json.encodeToString(body))
        }
    }

    suspend fun createNote(note: MisskeyNote): MisskeyNote {
        var data = note
        if (channelId != null) {
            data.channelId = channelId
        }

        val response = runBlocking { sendRequest("api/notes/create", data) }
        val responseJson = json.decodeFromString<CreatedNote>(response.bodyAsText())

        return responseJson.createdNote
    }

    fun close() {
        client.close()

    }
}