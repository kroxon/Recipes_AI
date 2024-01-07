package com.gpt.recipesai.data.network

import androidx.compose.runtime.collectAsState
import com.gpt.recipesai.data.api_keys.API
import com.gpt.recipesai.data.models.GptResponse
import com.gpt.recipesai.data.models.Message
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class GptCommunicator {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
    }

    private val textGenerationUrl = "https://api.openai.com/v1/chat/completions"

    val systemPrompt = Message(
        role = "system",
        content = "Jesteś pomocnym asystentem i podajesz w ładnej " + "formie przepis na śniadanie na podstawie składników w lodówce"
    )

    private val _historymessages = MutableStateFlow(listOf(systemPrompt))
    val historymessages = _historymessages.asStateFlow()



    suspend fun fetchGptResponse(promnt: String): Result<GptResponse> {
        val userPrompt = Message(role = "user", content = promnt)
        _historymessages.getAndUpdate { it + userPrompt }
        val requestBody = buildJsonObject {
            put("model", "gpt-3.5-turbo")
            putJsonArray("messages") {
                for (message in historymessages.value) {
                    addJsonObject {
                        put("role", message.role)
                        put("content", message.content)
                    }
                }
            }
        }

        return runCatching {
            val response = client.post<GptResponse>(textGenerationUrl) {
                header(HttpHeaders.Authorization, "Bearer ${API.apiKey}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                body = requestBody
            }
            val mess = response.choices?.first()?.message
            if (mess != null) {
                _historymessages.getAndUpdate { it + mess }
            }
            response
        }
    }
}



























