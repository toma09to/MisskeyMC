package com.toma09to.misskeymc.misskey

import kotlinx.serialization.Serializable

@Serializable
data class NullRequestBody(val hoge: String? = null) {
}