package com.toma09to.misskeymc.websocket

import com.toma09to.misskeymc.events.AsyncMisskeyChatEvent
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.logging.Logger

class WebSocketRunnable(
    private val logger: Logger,
    private val fqdn: String,
    private val channelId: String?,
    private val token: String,
) : BukkitRunnable() {
    private val client = HttpClient(Java) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val id = UUID.randomUUID().toString()

    private val session = runBlocking {
        client.webSocketSession {
            method = HttpMethod.Get
            url {
                protocol = URLProtocol.WSS
                host = fqdn
                port = 443
                pathSegments = listOf("streaming")
                parameters.append("i", token)
            }
        }
    }

    suspend fun connect() {
        session.outgoing.send(Frame.Text(
            json.encodeToString(connectChannelBody(id))
        ))
    }

    suspend fun disconnect() {
        session.outgoing.send(Frame.Text(
            json.encodeToString(disconnectChannelBody(id))
        ))
        withContext(Dispatchers.IO) {
            Thread.sleep(1000)
        }
        client.close()
    }

    override fun run() {
        logger.info(isCancelled.toString())
        if (!client.isActive) {
            cancel()
            return
        }

        runBlocking {
            session.incoming.let {
                when(val frame = it.tryReceive().getOrNull()) {
                    is Frame.Text -> {
                        val receivedText = frame.readText()
                        try {
                            val receivedData = json.decodeFromString<WebSocketResponse>(receivedText)

                            if (receivedData.body.body.channelId == channelId && !receivedData.body.body.user?.isBot!!) {
                                Bukkit.getPluginManager().callEvent(AsyncMisskeyChatEvent(receivedData.body.body))
                            } else if (receivedData.body.body.visibility == "specified") {
                                TODO("Authorization via Misskey")
                            }
                        } catch (e: Exception) {
                            logger.warning("Error while decoding $receivedText")
                            logger.warning(e.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}