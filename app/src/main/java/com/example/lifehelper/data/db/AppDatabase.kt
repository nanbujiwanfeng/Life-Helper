package com.example.lifehelper.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.lifehelper.data.db.dao.CourseDao
import com.example.lifehelper.data.db.dao.GoalDao
import com.example.lifehelper.data.db.dao.TransactionDao
import com.example.lifehelper.data.db.dao.TranslationDao
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.data.db.entity.Goal
import com.example.lifehelper.data.db.entity.Transaction
import com.example.lifehelper.data.db.entity.TranslationRecord

/**
 * 应用本地数据库
 * 版本变更时需递增 version 并提供 Migration 策略
 */
@Database(
    entities = [
        Goal::class,
        Course::class,
        Transaction::class,
        TranslationRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun courseDao(): CourseDao
    abstract fun transactionDao(): TransactionDao
    abstract fun translationDao(): TranslationDao

    companion object {
        private const val DB_NAME = "lifehelper.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}
