package com.toma09to.misskeymc.websocket

import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import java.util.logging.Logger

class WebSocketClient(
    logger: Logger,
    fqdn: String,
    channelId: String?,
    token: String
) {
    private var task = WebSocketRunnable(logger, fqdn, channelId, token)

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
        Thread.sleep(5_000)
    }
}