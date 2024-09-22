package com.toma09to.misskeymc

import com.toma09to.misskeymc.listeners.AsyncChatListener
import com.toma09to.misskeymc.listeners.MisskeyChatListener
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
            "" -> { null }
            else -> { str }
        }
        val useSSL = config.getBoolean("misskey.use-ssl")

        try {
            url = URI(urlString).toURL()
        } catch (e: URISyntaxException) {
            logger.warning("URL is invalid: $urlString")
            return
        }

        monitor = MisskeyMonitor(logger)
        misskey = MisskeyClient(logger, server.pluginManager, monitor!!, url, useSSL, token, channelId)

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

        runBlocking {
            misskey?.connectChannel()
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

        server.pluginManager.registerEvents(AsyncChatListener(toMisskeyPrefix, misskey!!), this)
        server.pluginManager.registerEvents(MisskeyChatListener(toMinecraftPrefix, server), this)

        logger.info("Enabled MisskeyMC")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        if (isClientStarted) {
            runBlocking {
                misskey?.disconnectChannel()
            }
        }

        logger.info("Disabled MisskeyMC")
    }
}
