package com.toma09to.misskeymc.listeners

import com.toma09to.misskeymc.database.SQLiteConnection
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class PlayerAuthorizationListener(private val db: SQLiteConnection, private val fqdn: String, private val botName: String) : Listener {
    @EventHandler
    fun onPlayerLogin(e: AsyncPlayerPreLoginEvent) {
        val uuid = e.uniqueId
        if (!db.isAuthorized(uuid)) {
            val token = db.createToken(uuid)
            val kickMessage = "このサーバーへの参加には認証が必要です\n" +
                    "https://$fqdn/@$botName へ以下のトークンをDMで送信してください\n" +
                    "\n" +
                    token
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage)
        }
    }
}