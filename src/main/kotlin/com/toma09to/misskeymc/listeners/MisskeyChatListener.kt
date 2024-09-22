package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.events.MisskeyChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler

class MisskeyChatListener(
    private val prefixFormat: String,
    private val server: Server
): Listener {
    @EventHandler
    fun onMisskeyChat(event: MisskeyChatEvent) {
        val prefix = prefixFormat
            .replace("%username%", event.note.user!!.username)
            .replace("%name%", event.note.user.name ?: event.note.user.username)
        val msg = prefix + event.note.text

        server.broadcast(Component.text(msg))
    }
}