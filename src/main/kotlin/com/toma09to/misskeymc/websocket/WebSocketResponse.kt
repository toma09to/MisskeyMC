package com.toma09to.misskeymc.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponse(val type: String, val body: WebSocketResponseBody)