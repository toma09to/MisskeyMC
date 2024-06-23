package com.toma09to.misskeymc.misskey

import kotlinx.serialization.Serializable

@Serializable
data class MisskeyNote (
    val id: String? = null,
    val user: MisskeyUser? = null,
    val text: String,
    val replyId: String? = null,
    val visibility: String? = null,
    var channelId: String? = null,
)