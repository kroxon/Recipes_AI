package com.gpt.recipesai.data.models.text


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("content")
    val content: String?,
    @SerialName("role")
    val role: String?
)