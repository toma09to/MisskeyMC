package com.toma09to.misskeymc.websocket

import com.toma09to.misskeymc.misskey.MisskeyNote
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponseBody(val id: String, val type: String, val body: MisskeyNote)