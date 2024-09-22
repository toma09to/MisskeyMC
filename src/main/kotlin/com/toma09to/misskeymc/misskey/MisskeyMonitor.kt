package com.toma09to.misskeymc.misskey

import org.bukkit.scheduler.BukkitRunnable
import java.util.logging.Logger

class MisskeyMonitor(val logger: Logger): BukkitRunnable() {
    var isMainConnected: Boolean = false
    var isChannelIdSet: Boolean = false
    var isChannelConnected: Boolean = false

    override fun run() {
        if (!isMainConnected) {
            logger.warning("Server does not connect to main channel now.")
        }
        if (isChannelIdSet && !isChannelConnected) {
            logger.warning("Server does not connect to the channel now.")
        }
    }
}