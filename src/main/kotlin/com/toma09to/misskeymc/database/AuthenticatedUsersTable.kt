package com.toma09to.misskeymc.database

import org.jetbrains.exposed.sql.Table

object AuthenticatedUsersTable: Table() {
    val minecraftUUID = uuid("mc_uuid")
    val minecraftUsername = varchar("mc_username", 16)
    val misskeyID = varchar("msky_id", 16)
    val misskeyUsername = varchar("msky_username", 20)
    val misskeyName = varchar("msky_name", 50).nullable()

    override val primaryKey = PrimaryKey(minecraftUUID)
}