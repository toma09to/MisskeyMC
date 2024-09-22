package com.toma09to.misskeymc.misskey

import com.toma09to.misskeymc.events.MisskeyChatEvent
import com.toma09to.misskeymc.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.plugin.PluginManager
import org.bukkit.scheduler.BukkitRunnable
import java.net.URL
import java.util.*
import java.util.logging.Logger

class MisskeyClient(
    private val logger: Logger,
    private val pluginManager: PluginManager,
    private val monitor: MisskeyMonitor,
    private val mskyURL: URL,
    private val ssl: Boolean,
    private val token: String,
    private val channelId: String?
): BukkitRunnable() {
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }

    private val wsSession = runBlocking {
        val path = generatePath("streaming")

        client.webSocketSession {
            url {
                protocol = if (ssl) URLProtocol.WSS else URLProtocol.WS
                host = mskyURL.host
                port = if (mskyURL.port == -1) mskyURL.defaultPort else mskyURL.port
                pathSegments = path
                headers {
                    bearerAuth(token)
                }
            }
        }
    }

    private val mainUUID = UUID.randomUUID().toString()
    private val chUUID = UUID.randomUUID().toString()
    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
    }
    private var myId: String? = null

    private var isCanceled = false

    fun verifyURL(): Boolean {
        val path = generatePath("server-info")

        val response = try {
            runBlocking {
                client.get {
                    url {
                        protocol = if (ssl) URLProtocol.HTTPS else URLProtocol.HTTP
                        host = mskyURL.host
                        port = if (mskyURL.port == -1) mskyURL.defaultPort else mskyURL.port
                        pathSegments = path
                    }
                }
            }
        } catch (e: Exception) {
            return false
        }

        return response.status == HttpStatusCode.OK
    }

    fun verifyToken(): Boolean {
        val path = generatePath("i")

        val response = runBlocking {
            client.post {
                url {
                    protocol = if (ssl) URLProtocol.HTTPS else URLProtocol.HTTP
                    host = mskyURL.host
                    port = if (mskyURL.port == -1) mskyURL.defaultPort else mskyURL.port
                    pathSegments = path
                }
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                    bearerAuth(token)
                }
                setBody("{}")
            }
        }

        if (response.status == HttpStatusCode.OK) {
            runBlocking {
                myId = json.decodeFromString<MisskeyUser>(response.body()).id
            }
            return true
        } else {
            return false
        }
    }

    fun verifyChannel(): Boolean {
        val path = generatePath("channels/unfollow")

        val response = runBlocking {
            client.post {
                url {
                    protocol = if (ssl) URLProtocol.HTTPS else URLProtocol.HTTP
                    host = mskyURL.host
                    port = if (mskyURL.port == -1) mskyURL.defaultPort else mskyURL.port
                    pathSegments = path
                }
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                    bearerAuth(token)
                }
                setBody("{\"channelId\":\"$channelId\"}")
            }
        }

        return response.status == HttpStatusCode.NoContent
    }

    private fun generatePath(endpoint: String): List<String> {
        val urlPath = mskyURL.path.drop(1).split('/')
        val endpointList = listOf("api") + endpoint.split('/')

        return if (urlPath[0] == "") {
            endpointList
        } else {
            urlPath + endpointList
        }
    }

    suspend fun connectChannel() {
        if (channelId != null) {
            monitor.isChannelIdSet = true
            wsSession.send(Frame.Text(json.encodeToString(webSocketConnectChannel(chUUID, channelId))))
        }
        wsSession.send(Frame.Text(json.encodeToString(webSocketConnectMain(mainUUID))))
    }

    suspend fun disconnectChannel() {
        wsSession.send(Frame.Text(json.encodeToString(webSocketDisconnect(mainUUID))))
        if (channelId != null) {
            wsSession.send(Frame.Text(json.encodeToString(webSocketDisconnect(chUUID))))
        }

        wsSession.send(Frame.Close())
        isCanceled = true
    }

    suspend fun sendNote(text: String): HttpResponse {
        val path = generatePath("notes/create")

        return client.post {
            url {
                protocol = if (ssl) URLProtocol.HTTPS else URLProtocol.HTTP
                host = mskyURL.host
                port = if (mskyURL.port == -1) mskyURL.defaultPort else mskyURL.port
                pathSegments = path
            }
            headers {
                append(HttpHeaders.ContentType, "application/json")
                bearerAuth(token)
            }
            setBody(json.encodeToString(MisskeyNote(
                text = text,
                channelId = channelId
            )))
        }
    }

    suspend fun sendReply(text: String, replyId: String) {
        wsSession.send(Frame.Text(""))
    }

    override fun run() {
        if (isCanceled) {
            cancel()
            return
        }

        runBlocking {
            wsSession.incoming.let {
                while (true) {
                    when (val frame = it.tryReceive().getOrNull()) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            try {
                                val data = json.decodeFromString<WebSocketJSON>(text)

                                when (data.type) {
                                    "connected" -> {
                                        if (data.body.id == mainUUID) {
                                            monitor.isMainConnected = true
                                        } else if (data.body.id == chUUID) {
                                            monitor.isChannelConnected = true
                                        }
                                    }
                                    "channel" -> {
                                        if (
                                            data.body.id == chUUID
                                            && data.body.type == "note"
                                            && data.body.body!!.user!!.id != myId
                                        ) {
                                            pluginManager.callEvent(MisskeyChatEvent(data.body.body))
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // unreadSpecifiedNoteが引っかかるので握りつぶす
                                // logger.warning("Error while decoding $text")
                                // logger.warning(e.message)
                            }
                        }
                        else -> { return@runBlocking }
                    }
                }
            }
        }
    }
}