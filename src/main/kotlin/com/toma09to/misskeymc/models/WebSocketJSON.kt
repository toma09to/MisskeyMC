package com.toma09to.misskeymc.models

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketJSON(
    val type: String,
    val body: WebSocketJSONBody
)

@Serializable
data class WebSocketJSONBody(
    val id: String,
    val type: String? = null,
    val channel: String? = null,
    val params: WebSocketJSONParams? = null,
    val body: MisskeyNote? = null,
    val pong: Boolean = true
)

@Serializable
data class WebSocketJSONParams(
    val channelId: String
)

fun webSocketConnectMain(id: String): WebSocketJSON {
    return WebSocketJSON(
        type = "connect",
        body = WebSocketJSONBody(
            id = id,
            channel = "main"
        )
    )
}

fun webSocketConnectChannel(id: String, channelId: String): WebSocketJSON {
    return WebSocketJSON(
        type = "connect",
        body = WebSocketJSONBody(
            id = id,
            channel = "channel",
            params = WebSocketJSONParams(channelId)
        )
    )
}

fun webSocketDisconnect(id: String): WebSocketJSON {
    return WebSocketJSON(
        type = "disconnect",
        body = WebSocketJSONBody(
            id = id
        )
    )
}

fun webSocketSendNote(id: String, note: MisskeyNote): WebSocketJSON {
    return WebSocketJSON(
        type = "channel",
        body = WebSocketJSONBody(
            id = id,
            type = "note",
            body = note
        )
    )
}