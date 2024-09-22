package com.toma09to.misskeymc.models

import kotlinx.serialization.Serializable

@Serializable
data class MisskeyNote(
    val id: String? = null,
    val user: MisskeyUser? = null,
    val text: String,
    val cw: String? = null,
    val visibility: String = "public",
    val localOnly: Boolean = true,
    val replyId: String? = null,
    val channelId: String? = null
)