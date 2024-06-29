package com.toma09to.misskeymc.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

class SQLiteConnection {
    private val db = Database.connect("jdbc:sqlite:plugins/MisskeyMC/misskeymc.db", "org.sqlite.JDBC")

    init {
        transaction(db) {
            SchemaUtils.create(AuthorizedUsers, Tokens)
        }
    }

    fun createToken(uuid: UUID): String {
        val token = SecureRandom().nextInt(1_000_000).toString().padStart(6, '0')
        val expiredAt = LocalDateTime.now().plusMinutes(30)
        transaction(db) {
            Tokens.insert {
                it[this.uuid] = uuid.toString()
                it[this.token] = token
                it[this.expiredAt] = expiredAt
            }
        }

        return token
    }

    fun authorizeUser(token: String, misskeyId: String): Boolean {
        var uuid: String? = null
        val now = LocalDateTime.now()
        transaction(db) {
            Tokens.selectAll()
                .where { (Tokens.token eq token) and (Tokens.expiredAt greaterEq now) }
                .forEach { uuid = it[Tokens.uuid] }
        }
        if (uuid == null) {
            return false
        }

        transaction(db) {
            AuthorizedUsers.insert {
                it[this.uuid] = uuid!!
                it[this.misskeyId] = misskeyId
            }
        }
        return true
    }

    fun isAuthorized(uuid: UUID): Boolean {
        var count = 0L
        transaction(db) {
            count = AuthorizedUsers.selectAll().where { AuthorizedUsers.uuid eq uuid.toString() }.count()
        }

        return count > 0
    }

    object AuthorizedUsers: Table() {
        val uuid = varchar("uuid", 36)
        val misskeyId = varchar("misskey_id", 16)

        override val primaryKey = PrimaryKey(uuid)
    }

    object Tokens: Table() {
        val uuid = varchar("uuid", 36)
        val token = varchar("token", 6)
        val expiredAt = datetime("expire_time")
    }
}