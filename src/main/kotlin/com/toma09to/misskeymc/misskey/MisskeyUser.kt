package com.toma09to.misskeymc.misskey

import kotlinx.serialization.Serializable

@Serializable
data class MisskeyUser(
    val id: String,
    val name: String?,
    val username: String,
    val isBot: Boolean,
)