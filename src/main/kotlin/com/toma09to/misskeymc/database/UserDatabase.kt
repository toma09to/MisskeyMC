package com.toma09to.misskeymc.database

import com.toma09to.misskeymc.models.MisskeyUser
import java.security.SecureRandom
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.*
import java.util.*
import java.util.logging.Logger
import kotlin.time.Duration.Companion.minutes

class UserDatabase(private val logger: Logger, private val uri: String) {
    private var db: Database? = null
    private val random = SecureRandom()

    fun connectDatabase(): Boolean {
        try {
            db = Database.connect("jdbc:sqlite:$uri", "org.sqlite.JDBC")
        } catch (e: Exception) {
            logger.warning(e.stackTraceToString())
            return false
        }
        return true
    }

    fun createTable() {
        transaction(db) {
            SchemaUtils.create(AuthenticatedUsersTable)
            SchemaUtils.create(OneTimePasswordsTable)
        }
    }

    fun checkPassword(pass: Int, user: MisskeyUser): Boolean {
        var result = false
        transaction(db) {
            val authenticatedUser = OneTimePasswordsTable.select(OneTimePasswordsTable.uuid, OneTimePasswordsTable.username).andWhere {
                OneTimePasswordsTable.password eq pass
            }.andWhere {
                OneTimePasswordsTable.expiredAt greater Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            }.andWhere {
                OneTimePasswordsTable.isUsed eq false
            }.map {
                it[OneTimePasswordsTable.uuid] to it[OneTimePasswordsTable.username]
            }

            result = authenticatedUser.isNotEmpty()
            if (result) {
                AuthenticatedUsersTable.insert {
                    it[minecraftUUID] = authenticatedUser[0].first
                    it[minecraftUsername] = authenticatedUser[0].second
                    it[misskeyID] = user.id
                    it[misskeyUsername] = user.username
                    it[misskeyName] = user.name
                }

                // Prevent other users from using used password
                OneTimePasswordsTable.update({ OneTimePasswordsTable.uuid eq authenticatedUser[0].first }) {
                    it[isUsed] = true
                }
            }
        }

        return result
    }

    fun generatePassword(playerUUID: UUID, playerName: String): Int {
        var pass: Int
        while (true) {
            pass = random.nextInt(100_000_000)

            // If password conflicts, regenerate pass
            var isValid = false
            transaction(db) {
                isValid = OneTimePasswordsTable.select(OneTimePasswordsTable.uuid, OneTimePasswordsTable.username).andWhere {
                    OneTimePasswordsTable.password eq pass
                }.andWhere {
                    OneTimePasswordsTable.expiredAt greater Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                }.andWhere {
                    OneTimePasswordsTable.isUsed eq false
                }.map {
                    it[OneTimePasswordsTable.uuid] to it[OneTimePasswordsTable.username]
                }.isEmpty()
            }

            if (isValid) {
                break
            }
        }

        transaction(db) {
            OneTimePasswordsTable.insert {
                it[uuid] = playerUUID
                it[username] = playerName
                it[password] = pass
                it[expiredAt] = (Clock.System.now() + 30.minutes).toLocalDateTime(TimeZone.currentSystemDefault())
                it[isUsed] = false
            }
        }

        return pass
    }

    fun isAuthorized(uuid: UUID): Boolean {
        var result = false
        transaction(db) {
            result = AuthenticatedUsersTable.select(AuthenticatedUsersTable.minecraftUUID).where {
                AuthenticatedUsersTable.minecraftUUID eq uuid
            }.map{}.isNotEmpty()
        }

        return result
    }
}