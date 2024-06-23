package com.toma09to.misskeymc.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketDisconnectChannel(val type: String, val body: Body) {
    @Serializable
    data class Body(val id: String)
}

fun disconnectChannelBody(id: String): WebSocketDisconnectChannel {
    return WebSocketDisconnectChannel("disconnect", WebSocketDisconnectChannel.Body(id))
}