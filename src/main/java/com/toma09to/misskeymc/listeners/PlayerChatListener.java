package com.toma09to.misskeymc.listeners;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import com.toma09to.misskeymc.api.MisskeyHttpClient;

public class PlayerChatListener implements Listener {
    private final MisskeyHttpClient misskey;
    private final String chatMessageTemplate;

    public PlayerChatListener(MisskeyHttpClient misskey, String chat) {
        this.misskey = misskey;
        this.chatMessageTemplate = chat;
    }

    @EventHandler
    public void onPlayerSendChat(AsyncChatEvent event) {
        final String playerName = event.getPlayer().getName();
        final String content = PlainTextComponentSerializer.plainText().serialize(event.message());
        final String message = chatMessageTemplate.replace("%player%", playerName).replace("%content%", content);

        misskey.sendPost(message);
    }
}
