package com.toma09to.misskeymc.events;

import com.toma09to.misskeymc.misskey.MisskeyNote;
import com.toma09to.misskeymc.misskey.MisskeyUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class AsyncMisskeyChatEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final MisskeyNote note;

    public AsyncMisskeyChatEvent(MisskeyNote note) {
        super(true);
        this.note = note;
    }

    public String getName() {
        MisskeyUser user = note.getUser();
        return user != null ? user.getName() : null;
    }
    public String getUserName() {
        MisskeyUser user = note.getUser();
        return user != null ? user.getUsername() : null;
    }
    public String getText() {
        return note.getText();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
