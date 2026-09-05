package com.example.lifehelper.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * 应用语言（Locale）切换工具
 * 配合 MainActivity.attachBaseContext 使用，实现界面语言切换
 */
object LocaleHelper {

    /**
     * 根据存储的语言偏好返回 Locale。
     * 目前内置中文（默认）与英文资源，其他语言回退系统默认。
     */
    fun resolveLocale(context: Context): Locale {
        val lang = context.getSharedPreferences("lifehelper_prefs", Context.MODE_PRIVATE)
            .getString("language", "system") ?: "system"
        return when (lang) {
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "en" -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }

    /** 将指定 Locale 包装到 Context 中 */
    fun wrap(context: Context, locale: Locale): Context {
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return context.createConfigurationContext(config)
    }
}
