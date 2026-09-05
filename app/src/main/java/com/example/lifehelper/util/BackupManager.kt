package com.example.lifehelper.util

import android.content.Context
import android.net.Uri
import com.example.lifehelper.data.db.AppDatabase
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.data.db.entity.Goal
import com.example.lifehelper.data.db.entity.Transaction
import com.example.lifehelper.data.db.entity.TranslationRecord
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * 数据备份与恢复：将全部业务数据导出为 JSON 文件 / 从 JSON 文件恢复
 * 使用 Storage Access Framework（SAF），无需存储运行时权限
 */
class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val db get() = AppDatabase.getInstance(context)

    /** 备份数据 DTO */
    data class BackupData(
        val version: Int = 1,
        val exportedAt: Long = System.currentTimeMillis(),
        val goals: List<Goal> = emptyList(),
        val courses: List<Course> = emptyList(),
        val transactions: List<Transaction> = emptyList(),
        val translationRecords: List<TranslationRecord> = emptyList()
    )

    /** 导出所有数据到指定 Uri（JSON） */
    suspend fun export(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val data = BackupData(
                goals = db.goalDao().getAllGoals().first(),
                courses = db.courseDao().getAllCourses().first(),
                transactions = db.transactionDao().getAllTransactions().first(),
                translationRecords = db.translationDao().getAllRecords().first()
            )
            val json = gson.toJson(data)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out).use { writer -> writer.write(json) }
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** 从指定 Uri 导入并恢复所有数据（先清空后写入） */
    suspend fun import(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).use { it.readText() }
            } ?: return@withContext false

            val data = gson.fromJson(json, BackupData::class.java) ?: return@withContext false

            val goalDao = db.goalDao()
            val courseDao = db.courseDao()
            val transactionDao = db.transactionDao()
            val translationDao = db.translationDao()

            goalDao.deleteAll()
            courseDao.deleteAll()
            transactionDao.deleteAll()
            translationDao.deleteAll()

            // 重置 id，让 Room 重新自动生成，避免主键冲突
            data.goals.forEach { goalDao.insert(it.copy(id = 0)) }
            data.courses.forEach { courseDao.insert(it.copy(id = 0)) }
            data.transactions.forEach { transactionDao.insert(it.copy(id = 0)) }
            data.translationRecords.forEach { translationDao.insert(it.copy(id = 0)) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** 清空全部业务数据（数据库四张表） */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        try {
            db.goalDao().deleteAll()
            db.courseDao().deleteAll()
            db.transactionDao().deleteAll()
            db.translationDao().deleteAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 导出账目为 CSV（可选功能，记账模块使用） */
    suspend fun exportTransactionsCsv(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val list = db.transactionDao().getAllTransactions().first()
            val sb = StringBuilder("类型,金额,分类,日期,备注\n")
            list.forEach { t ->
                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA)
                    .format(java.util.Date(t.date))
                sb.append(
                    "${if (t.type == Transaction.TYPE_INCOME) "收入" else "支出"}," +
                        "${t.amount},${t.category},${date},${t.note.replace(",", "，")}\n"
                )
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
