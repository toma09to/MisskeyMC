package com.toma09to.misskeymc.websocket

import com.toma09to.misskeymc.database.SQLiteConnection
import com.toma09to.misskeymc.events.AsyncMisskeyChatEvent
import com.toma09to.misskeymc.misskey.MisskeyClient
import com.toma09to.misskeymc.misskey.MisskeyNote
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.logging.Logger

class WebSocketRunnable(
    private val logger: Logger,
    private val db: SQLiteConnection,
    private val msky: MisskeyClient,
    private val fqdn: String,
    private val channelId: String?,
    private val token: String,
    private val botName: String,
) : BukkitRunnable() {
    private val client = HttpClient(Java) {
        install(WebSockets) {
            pingInterval = 20_000
        }
        install(Logging)
    }
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val id = UUID.randomUUID().toString()

    private var session: WebSocketSession? = null

    suspend fun connect() {
        session = client.webSocketSession {
            method = HttpMethod.Get
            url {
                protocol = URLProtocol.WSS
                host = fqdn
                port = 443
                pathSegments = listOf("streaming")
                parameters.append("i", token)
            }
        }

        session?.outgoing?.send(Frame.Text(
            json.encodeToString(connectChannelBody(id))
        ))
    }

    suspend fun disconnect() {
        session?.outgoing?.send(Frame.Text(
            json.encodeToString(disconnectChannelBody(id)))
        )
        client.close()
    }

    override fun run() {
        runBlocking {
            if (!client.isActive) {
                connect()
                return@runBlocking
            }

            session?.incoming?.let {
                when(val frame = it.tryReceive().getOrNull()) {
                    is Frame.Text -> {
                        val receivedText = frame.readText()
                        try {
                            val receivedData = json.decodeFromString<WebSocketResponse>(receivedText)
                            val receivedPost = receivedData.body.body

                            if (receivedPost.channelId == channelId && !receivedPost.user?.isBot!!) {
                                Bukkit.getPluginManager().callEvent(AsyncMisskeyChatEvent(receivedData.body.body))
                            } else if (receivedPost.visibility == "specified") {
                                val receivedToken = receivedPost.text
                                    .replace("@$botName", "")
                                    .trim()
                                if (db.authorizeUser(receivedToken, receivedPost.user!!.id)) {
                                    msky.createNote(MisskeyNote(text = "認証に成功しました。", replyId = receivedPost.id, visibility = "specified"))
                                } else {
                                    msky.createNote(MisskeyNote(text = "認証に失敗しました。もう一度お試しください。", replyId = receivedPost.id, visibility = "specified"))
                                }
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