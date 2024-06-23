package com.toma09to.misskeymc.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketConnectChannel(val type: String, val body: Body) {
    @Serializable
    data class Body(val channel: String, val id: String)
}

fun connectChannelBody(id: String): WebSocketConnectChannel {
    return WebSocketConnectChannel("connect", WebSocketConnectChannel.Body("homeTimeline", id))
}