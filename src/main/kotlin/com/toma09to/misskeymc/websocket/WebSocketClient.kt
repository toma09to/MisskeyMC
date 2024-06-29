package com.toma09to.misskeymc.websocket

import com.toma09to.misskeymc.database.SQLiteConnection
import com.toma09to.misskeymc.misskey.MisskeyClient
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import java.util.logging.Logger

class WebSocketClient(
    logger: Logger,
    db: SQLiteConnection,
    msky: MisskeyClient,
    fqdn: String,
    botName: String,
    channelId: String?,
    token: String,
) {
    private val task = WebSocketRunnable(logger, db, msky, fqdn, channelId, token, botName)

    fun connect() {
        runBlocking {
            task.connect()
        }
        task.runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("MisskeyMC")!!, 0L, 20L)
    }

    fun close() {
        runBlocking {
            task.disconnect()
        }
    }
}