package com.toma09to.misskeymc.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object OneTimePasswordsTable: Table() {
    val uuid = uuid("mc_uuid")
    val username = varchar("mc_username", 16)
    val password = integer("password")
    val expiredAt = datetime("expired_at")
    val isUsed = bool("is_used")
}