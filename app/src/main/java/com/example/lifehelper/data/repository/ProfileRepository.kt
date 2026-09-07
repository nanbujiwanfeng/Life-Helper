package com.example.lifehelper.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户资料与设置仓库，基于 SharedPreferences 存储
 * 存储内容：头像(base64)、昵称、个性签名、主题模式、语言、通知开关、月预算
 */
class ProfileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 头像（base64 字符串，空则使用默认头像）
    fun getAvatar(): String = prefs.getString(KEY_AVATAR, "") ?: ""
    fun setAvatar(value: String) = prefs.edit().putString(KEY_AVATAR, value).apply()

    fun getNickname(): String = prefs.getString(KEY_NICKNAME, "") ?: ""
    fun setNickname(value: String) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    fun getSignature(): String = prefs.getString(KEY_SIGNATURE, "") ?: ""
    fun setSignature(value: String) = prefs.edit().putString(KEY_SIGNATURE, value).apply()

    // 主题：light / dark / system
    fun getThemeMode(): String = prefs.getString(KEY_THEME, "system") ?: "system"
    fun setThemeMode(value: String) = prefs.edit().putString(KEY_THEME, value).apply()

    // 应用语言：zh / en / system
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "system") ?: "system"
    fun setLanguage(value: String) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    // 通知开关
    fun isNotificationEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION, true)
    fun setNotificationEnabled(value: Boolean) =
        prefs.edit().putBoolean(KEY_NOTIFICATION, value).apply()

    // 月预算（0 表示未设置）
    fun getMonthlyBudget(): Double = prefs.getFloat(KEY_BUDGET, 0f).toDouble()
    fun setMonthlyBudget(value: Double) = prefs.edit().putFloat(KEY_BUDGET, value.toFloat()).apply()

    // 当前教学周（第几周，1 起）
    fun getCurrentWeek(): Int = prefs.getInt(KEY_WEEK, 1)
    fun setCurrentWeek(value: Int) = prefs.edit().putInt(KEY_WEEK, value).apply()

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val PREFS_NAME = "lifehelper_prefs"
        private const val KEY_AVATAR = "avatar"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_SIGNATURE = "signature"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATION = "notification_enabled"
        private const val KEY_BUDGET = "monthly_budget"
        private const val KEY_WEEK = "current_week"
    }
}
