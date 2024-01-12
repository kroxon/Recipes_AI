package com.gpt.recipesai.data.models.image


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GptImageResponse(
    @SerialName("created")
    val created: Int?,
    @SerialName("data")
    val `data`: List<Data?>?
)