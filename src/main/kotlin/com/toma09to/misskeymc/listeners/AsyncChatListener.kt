package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.misskey.MisskeyClient
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler

class AsyncChatListener(
    private val prefixFormat: String,
    private val msky: MisskeyClient
): Listener {
    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val prefix = prefixFormat.replace("%player%", event.player.name)
        val msg = prefix + (event.message() as TextComponent).content()

        runBlocking {
            msky.sendNote(msg)
        }
    }
}