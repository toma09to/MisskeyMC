package com.toma09to.misskeymc.models

import kotlinx.serialization.Serializable

@Serializable
data class MisskeyUser(
    val id: String,
    val name: String?,
    val username: String,
    val isBot: Boolean
)