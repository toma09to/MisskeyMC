package com.toma09to.misskeymc

import org.bukkit.plugin.java.JavaPlugin

class MisskeyMC : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Enabled MisskeyMC")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Disabled MisskeyMC")
    }
}
