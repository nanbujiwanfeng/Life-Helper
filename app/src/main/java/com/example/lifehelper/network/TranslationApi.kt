package com.example.lifehelper.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 免费翻译 API 接口（默认使用 MyMemory，无需密钥）
 *
 * 如希望替换为其他翻译服务（如需要密钥的 Google/DeepL 等），
 * 请参考 TranslationService.kt 中的 TODO 说明。
 */
interface TranslationApi {

    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): TranslationResponse
}

/** MyMemory API 响应体 */
data class TranslationResponse(
    @SerializedName("responseStatus") val status: Int? = null,
    @SerializedName("responseData") val data: ResponseData? = null
) {
    val isOk: Boolean get() = (status ?: 0) == 200
}

data class ResponseData(
    @SerializedName("translatedText") val translatedText: String? = null
)
