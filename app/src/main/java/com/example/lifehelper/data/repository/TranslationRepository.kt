package com.example.lifehelper.data.repository

import com.example.lifehelper.data.db.dao.TranslationDao
import com.example.lifehelper.data.db.entity.TranslationRecord
import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val dao: TranslationDao) {

    fun getAllRecords(): Flow<List<TranslationRecord>> = dao.getAllRecords()

    suspend fun addRecord(record: TranslationRecord): Long = dao.insert(record)

    suspend fun deleteRecord(record: TranslationRecord) = dao.delete(record)

    suspend fun deleteAll() = dao.deleteAll()
}
