package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.misskey.MisskeyClient
import com.toma09to.misskeymc.misskey.MisskeyNote
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class PlayerChatListener(private val client: MisskeyClient, private val format: String) : Listener {
    @EventHandler
    fun onPlayerSendChat(e: AsyncPlayerChatEvent) {
        val content = format
            .replace("%username%", e.player.name)
            .replace("%message%", e.message)

        runBlocking {
            client.createNote(MisskeyNote(text = content, visibility = "public"))
        }
    }
}