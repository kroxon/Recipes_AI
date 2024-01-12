package com.gpt.recipesai.data.models.text


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Choice(
    @SerialName("finish_reason")
    val finishReason: String?,
    @SerialName("index")
    val index: Int?,
    @SerialName("logprobs")
    val logprobs: Map<String, Double>?,
    @SerialName("message")
    val message: Message?
)