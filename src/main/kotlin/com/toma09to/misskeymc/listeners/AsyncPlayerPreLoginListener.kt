package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.database.UserDatabase
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener(private val db: UserDatabase): Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (
            !db.isAuthorized(event.uniqueId)
        ) {
            val pass = db.generatePassword(event.uniqueId, event.name)

            event.kickMessage(
                Component.text("このサーバーに参加するにはMisskeyによる認証が必要です", NamedTextColor.GREEN)
                    .append(Component.newline())
                    .append(Component.text("以下の数字をBotへダイレクトで送信してください", NamedTextColor.LIGHT_PURPLE))
                    .append(Component.newline())
                    .append(Component.text(pass.toString().padStart(8, '0'), NamedTextColor.RED))
            )
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST
        }
    }
}