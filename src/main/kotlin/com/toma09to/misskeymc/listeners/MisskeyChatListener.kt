package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.events.AsyncMisskeyChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MisskeyChatListener(private val format: String) : Listener {
    @EventHandler
    fun onMisskeySendChat(e: AsyncMisskeyChatEvent) {
        val name = if (e.name != null) {
            e.name
        } else {
            e.userName
        }

        val content = format
            .replace("%username%", e.userName!!)
            .replace("%name%", name!!)
            .replace("%message%", e.text)

        Bukkit.broadcastMessage(content)
    }
}