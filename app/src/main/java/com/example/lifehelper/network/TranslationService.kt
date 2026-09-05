package com.example.lifehelper.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 翻译服务单例（Retrofit + OkHttp）
 *
 * 默认使用 MyMemory 免费 API（无需密钥，支持多语言互译）。
 * 若需更换为需要密钥的服务，请在下方 TODO 处填入常量并在对应接口实现中传递。
 */
object TranslationService {

    // TODO: 如使用需要密钥的翻译 API，请在此定义密钥常量（切勿提交真实密钥到仓库）
    // private const val API_KEY = "your_api_key_here"

    private const val BASE_URL = "https://api.mymemory.translated.net/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: TranslationApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TranslationApi::class.java)
    }

    /** 支持的语言列表（显示名 -> API 语言代码） */
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
}
