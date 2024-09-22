package com.toma09to.misskeymc.events

import com.toma09to.misskeymc.models.MisskeyNote
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MisskeyChatEvent(val note: MisskeyNote): Event() {
    override fun getHandlers(): HandlerList {
        return MisskeyChatEvent.handlers
    }

    companion object {
        @JvmStatic
        private val handlers: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }
}