package com.gpt.recipesai.data.models.image


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Data(
    @SerialName("url")
    val url: String?
)