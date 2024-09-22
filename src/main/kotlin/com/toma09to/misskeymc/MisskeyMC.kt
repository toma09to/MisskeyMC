package com.toma09to.misskeymc

import com.toma09to.misskeymc.database.UserDatabase
import com.toma09to.misskeymc.listeners.*
import com.toma09to.misskeymc.misskey.MisskeyClient
import com.toma09to.misskeymc.misskey.MisskeyMonitor
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class MisskeyMC : JavaPlugin() {
    private var misskey: MisskeyClient? = null
    private var monitor: MisskeyMonitor? = null
    private var isClientStarted: Boolean = false

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        val isAuthRequired = config.getBoolean("auth.require-authentication")
        var database: UserDatabase? = null
        if (isAuthRequired) {
            database = UserDatabase(logger, "plugins/MisskeyMC/misskeymc.db")
            if (!database.connectDatabase()) {
                logger.warning("Failed to connect database.")
                logger.warning("Server operates as if authentication is NOT required.")
                database = null
            } else {
                database.createTable()
                server.pluginManager.registerEvents(AsyncPlayerPreLoginListener(database), this)
            }
        }

        val url: URL
        val urlString = when (val str = config.getString("misskey.url")) {
            is String -> { str }
            else -> {
                logger.warning("URL is not set!")
                return
            }
        }
        val token = when (val str = config.getString("misskey.token")) {
            is String -> { str }
            else -> {
                logger.warning("Token is not set!")
                return
            }
        }
        val channelId = when (val str = config.getString("misskey.channel-id")) {
            is String -> { str }
            else -> {
                logger.warning("Channel ID is not set!")
                return
            }
        }
        val useSSL = config.getBoolean("misskey.use-ssl")

        try {
            url = URI(urlString).toURL()
        } catch (e: URISyntaxException) {
            logger.warning("URL is invalid: $urlString")
            return
        }

        monitor = MisskeyMonitor(logger)
        misskey = MisskeyClient(server.pluginManager, monitor!!, url, useSSL, token, channelId, database)

        // verify parameters
        if (misskey?.verifyURL() != true) {
            logger.warning("$urlString is unavailable (or isn't Misskey server).")
            logger.warning("Verify that URL is correct.")
            return
        }
        if (misskey?.verifyToken() != true) {
            logger.warning("Token does not work.")
            logger.warning("Verify that token is correct and has appropriate permissions.")
            return
        }
        if (misskey?.verifyChannel() != true) {
            logger.warning("Not found the channel.")
            logger.warning("Verify that channel ID is correct.")
            return
        }

        val isWebSocketConnected = runBlocking { misskey?.connectWebSocket() }
        if (isWebSocketConnected == true) {
            runBlocking {
                misskey?.connectChannel()
            }
        } else {
            logger.warning("Cannot connect the server over WebSocket.")
            logger.warning("Verify that URL is correct.")
            return
        }
        misskey?.runTaskTimer(this, 0L, 20L)
        monitor?.runTaskTimer(this, 60 * 20L, 60 * 20L)
        isClientStarted = true

        val toMisskeyPrefix = when (val str = config.getString("chat.minecraft-to-misskey-prefix")) {
            is String -> { str }
            else -> { "" }
        }
        val toMinecraftPrefix = when (val str = config.getString("chat.misskey-to-minecraft-prefix")) {
            is String -> { str }
            else -> { "" }
        }
        val joinFormat = config.getString("chat.join-message-format")
        val quitFormat = config.getString("chat.quit-message-format")

        server.pluginManager.registerEvents(AsyncChatListener(toMisskeyPrefix, misskey!!), this)
        server.pluginManager.registerEvents(MisskeyChatListener(toMinecraftPrefix, server), this)
        if (joinFormat != null) {
            server.pluginManager.registerEvents(PlayerJoinListener(joinFormat, misskey!!), this)
        }
        if (quitFormat != null) {
            server.pluginManager.registerEvents(PlayerQuitListener(quitFormat, misskey!!), this)
        }

        val serverStartMessage = config.getString("chat.server-start-message")
        if (serverStartMessage != null) {
            runBlocking {
                misskey?.sendNote(serverStartMessage)
            }
        }

        logger.info("Enabled MisskeyMC")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        if (isClientStarted) {
            runBlocking {
                misskey?.disconnectChannel()
            }
        }

        val serverStopMessage = config.getString("chat.server-stop-message")
        if (serverStopMessage != null) {
            runBlocking {
                misskey?.sendNote(serverStopMessage)
            }
        }
        logger.info("Disabled MisskeyMC")
    }
}
