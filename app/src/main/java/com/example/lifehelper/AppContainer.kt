package com.example.lifehelper

import android.content.Context
import com.example.lifehelper.data.db.AppDatabase
import com.example.lifehelper.data.repository.CourseRepository
import com.example.lifehelper.data.repository.GoalRepository
import com.example.lifehelper.data.repository.ProfileRepository
import com.example.lifehelper.data.repository.TransactionRepository
import com.example.lifehelper.data.repository.TranslationRepository

/**
 * 手动依赖注入容器（未使用 Hilt，保持构建简单）
 * 持有数据库、各仓库单例，供 ViewModel Factory 获取
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val goalRepository = GoalRepository(database.goalDao())
    val courseRepository = CourseRepository(database.courseDao())
    val transactionRepository = TransactionRepository(database.transactionDao())
    val translationRepository = TranslationRepository(database.translationDao())
    val profileRepository = ProfileRepository(context)
}
