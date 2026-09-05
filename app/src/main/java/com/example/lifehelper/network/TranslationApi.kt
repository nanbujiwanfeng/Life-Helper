package com.example.lifehelper.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * DeepSeek 翻译接口（OpenAI 兼容的 chat/completions 端点）
 *
 * 通过对话方式实现翻译：system 指定翻译规则，user 携带待翻译文本。
 */
interface TranslationApi {

    @POST("chat/completions")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

/** 对话请求体 */
data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.3,
    val stream: Boolean = false
)

data class ChatMessage(
    val role: String,
    val content: String
)

/** 对话响应体 */
data class ChatResponse(
    val choices: List<ChatChoice>? = null
)

data class ChatChoice(
    val message: ChatMessage? = null
)
