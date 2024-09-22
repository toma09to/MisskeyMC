package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.misskey.MisskeyClient
import kotlinx.coroutines.runBlocking
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val format: String,
    private val msky: MisskeyClient
): Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val message = format.replace("%player%", event.player.name)

        runBlocking {
            msky.sendNote(message)
        }
    }
}