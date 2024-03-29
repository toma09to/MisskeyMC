package com.toma09to.misskeymc.listeners;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.toma09to.misskeymc.api.MisskeyLogger;
import com.toma09to.misskeymc.database.UsersDatabase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class PlayerCheckAuthorizedListener implements Listener {
    private final UsersDatabase database;
    private final String serverUrl;
    private final String botName;
    private final String contact;

    public PlayerCheckAuthorizedListener(UsersDatabase database, String serverUrl, String botName, String contact) {
        this.database = database;
        this.serverUrl = serverUrl;
        this.botName = botName;
        this.contact = contact;
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        PlayerProfile player = event.getPlayerProfile();
        try {
            if (!database.isAuthorized(player.getId().toString())) {
                String token = database.generateToken(player);
                final Component kickMessage = getKickMessage(token);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
            }
        } catch (SQLException e) {
            new MisskeyLogger(Bukkit.getLogger()).warning(e.getMessage());
            final Component errorMessage = Component.text("エラーが発生しました").color(TextColor.color(0xFF5555))
                    .appendNewline()
                    .append(Component.text(contact + "までご連絡ください"));
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, errorMessage);
        }
    }

    @NotNull
    private Component getKickMessage(String token) {
        String url = serverUrl + "/@" + botName;

        return Component.text("このサーバーへの参加には認証が必要です").decorate(TextDecoration.BOLD)
                .appendNewline()
                .append(Component.text(url).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl(url)))
                .append(Component.text("へ以下のトークンをDMで送ってください"))
                .appendNewline()
                .append(Component.text(token).color(TextColor.color(0x55FF55)));
    }
}
