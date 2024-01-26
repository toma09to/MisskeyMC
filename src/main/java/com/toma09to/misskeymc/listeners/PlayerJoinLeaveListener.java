package com.toma09to.misskeymc.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.toma09to.misskeymc.api.MisskeyHttpClient;

public class PlayerJoinLeaveListener implements Listener {
    private final MisskeyHttpClient misskey;
    private final String serverUrl;
    private final String joinMessageTemplate;
    private final String quitMessageTemplate;

    public PlayerJoinLeaveListener(MisskeyHttpClient misskey, String serverUrl, String join, String quit) {
        this.misskey = misskey;
        this.serverUrl = serverUrl;
        this.joinMessageTemplate = join;
        this.quitMessageTemplate = quit;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player joinPlayer = event.getPlayer();
        final String playerName = joinPlayer.getName();
        final String message = joinMessageTemplate.replace("%player%", playerName);
        final Component misskeyInfo = MiniMessage.miniMessage().deserialize(
                "あなたも<green><u><click:open_url:'" + serverUrl + "'>Misskeyサーバー</click></u></green>に参加しませんか？"
        );

        joinPlayer.sendMessage(misskeyInfo);
        misskey.sendPost(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final String playerName = event.getPlayer().getName();
        final String message = quitMessageTemplate.replace("%player%", playerName);

        misskey.sendPost(message);
    }
}