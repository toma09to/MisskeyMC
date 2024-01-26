package com.toma09to.misskeymc;

import com.toma09to.misskeymc.listeners.PlayerJoinLeaveListener;
import com.toma09to.misskeymc.listeners.PlayerChatListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.toma09to.misskeymc.api.MisskeyHttpClient;

public final class MisskeyMC extends JavaPlugin {
    private MisskeyHttpClient misskey;
    private String enabledMessage;
    private String disabledMessage;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);

        String host = getConfig().getString("misskey.host");
        boolean useSsl = getConfig().getBoolean("misskey.useSsl");
        String token = getConfig().getString("misskey.token");
        String visibility = getConfig().getString("misskey.visibility");
        boolean localOnly = getConfig().getBoolean("misskey.localOnly");
        String channelId = getConfig().getString("misskey.channelId");
        String prefix = getConfig().getString("misskey.prefix");
        boolean isDebug = getConfig().getBoolean("misskey.debug");
        String serverUrl = getConfig().getString("misskey.serverUrl");

        String joinMessage = getConfig().getString("message.joinMessage");
        String quitMessage = getConfig().getString("message.quitMessage");
        this.enabledMessage = getConfig().getString("message.enabledMessage");
        this.disabledMessage = getConfig().getString("message.disabledMessage");
        String chatMessage = getConfig().getString("message.chatMessage");

        misskey = new MisskeyHttpClient(host, useSsl, token, visibility, localOnly, channelId, prefix, isDebug);

        Bukkit.getServer().getPluginManager().registerEvents(
                new PlayerJoinLeaveListener(misskey, serverUrl, joinMessage, quitMessage),
                this
        );
        Bukkit.getServer().getPluginManager().registerEvents(
                new PlayerChatListener(misskey, chatMessage),
                this
        );

        misskey.sendPost(enabledMessage);
    }

    @Override
    public void onDisable() {
        misskey.sendPost(disabledMessage);
    }
}
