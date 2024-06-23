package com.toma09to.misskeymc

import com.toma09to.misskeymc.listeners.MisskeyChatListener
import com.toma09to.misskeymc.listeners.PlayerChatListener
import com.toma09to.misskeymc.misskey.MisskeyClient
import com.toma09to.misskeymc.misskey.MisskeyNote
import com.toma09to.misskeymc.websocket.WebSocketClient
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class MisskeyMC : JavaPlugin() {
    private var channelId: String? = null
    private var toMisskeyServerFormat: String = ""
    private var client: MisskeyClient = MisskeyClient("", null, "")
    private var wsClient: WebSocketClient? = null

    override fun onEnable() {
        saveDefaultConfig()

        val fqdn = config.getString("misskey.server-fqdn")
        channelId = config.getString("misskey.channel-id")
        val token = config.getString("misskey.token")
        val toMisskeyChatFormat = config.getString("misskey.chat-message-format")
        val toMisskeyServerFormatNullable = config.getString("misskey.server-message-format")

        if (fqdn == null || token == null || toMisskeyChatFormat == null || toMisskeyServerFormatNullable == null) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }
        toMisskeyServerFormat = toMisskeyServerFormatNullable

        client = MisskeyClient(fqdn, channelId, token)
        wsClient = WebSocketClient(logger, fqdn, channelId, token)

        server.pluginManager.registerEvents(PlayerChatListener(client, toMisskeyChatFormat), this)
        server.pluginManager.registerEvents(MisskeyChatListener(toMisskeyChatFormat), this)

        wsClient?.connect()

        val enabledMessage = toMisskeyServerFormat.replace("%message%", "サーバーが起動しました")
        runBlocking {
            client.createNote(MisskeyNote(channelId = channelId, text = enabledMessage, visibility = "public"))
        }
        logger.info("Enabled MisskeyMC")
    }

    override fun onDisable() {
        client.close()
        wsClient?.close()

        val disabledMessage = toMisskeyServerFormat.replace("%message%", "サーバーが終了しました")
        runBlocking {
            client.createNote(MisskeyNote(channelId = channelId, text = disabledMessage, visibility = "public"))
        }

        logger.info("Disabled MisskeyMC")
    }
}
