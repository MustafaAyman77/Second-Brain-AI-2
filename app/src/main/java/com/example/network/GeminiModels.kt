package com.example.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<JsonObject>? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@Serializable
data class ResponseFormatText(
    val mimeType: String,
    val schema: JsonObject? = null
)

@Serializable
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseModalities: List<String>? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)
