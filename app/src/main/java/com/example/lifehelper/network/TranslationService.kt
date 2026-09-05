package com.example.lifehelper.network

import com.example.lifehelper.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * DeepSeek 翻译服务单例（Retrofit + OkHttp）
 *
 * 密钥通过 Gradle 从本地 `secrets.properties` 注入到 BuildConfig，
 * 该文件已加入 .gitignore，不会提交到公开仓库。
 */
object TranslationService {

    private const val BASE_URL = "https://api.deepseek.com/"

    /** 是否已配置密钥 */
    val isConfigured: Boolean get() = BuildConfig.DEEPSEEK_API_KEY.isNotBlank()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: TranslationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
    }

    /** 支持的语言列表（显示名 -> 代码） */
    val languages: List<Pair<String, String>> = listOf(
        "自动检测" to "auto",
        "中文（简体）" to "zh-CN",
        "英语" to "en",
        "日语" to "ja",
        "韩语" to "ko",
        "法语" to "fr",
        "德语" to "de",
        "西班牙语" to "es",
        "俄语" to "ru",
        "意大利语" to "it",
        "葡萄牙语" to "pt",
        "阿拉伯语" to "ar"
    )

    /** 语言代码 -> 显示名 */
    fun languageName(code: String): String =
        languages.firstOrNull { it.second == code }?.first ?: code

    /** 构建翻译提示词（system, user） */
    fun buildPrompt(sourceCode: String, targetCode: String, text: String): Pair<String, String> {
        val target = languageName(targetCode)
        val system = "你是一名专业翻译。只输出翻译结果本身，不要任何解释、注释、前后缀或引号。"
        val user = if (sourceCode == "auto") {
            "请将以下文本翻译成${target}：\n\n$text"
        } else {
            "请将以下${languageName(sourceCode)}文本翻译成${target}：\n\n$text"
        }
        return system to user
    }

    /** 构建对话请求体 */
    fun buildRequest(sourceCode: String, targetCode: String, text: String): ChatRequest {
        val (system, user) = buildPrompt(sourceCode, targetCode, text)
        return ChatRequest(
            messages = listOf(
                ChatMessage(role = "system", content = system),
                ChatMessage(role = "user", content = user)
            )
        )
    }
}
