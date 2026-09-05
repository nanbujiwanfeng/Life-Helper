package com.example.lifehelper.data.repository

import com.example.lifehelper.data.db.dao.TransactionDao
import com.example.lifehelper.data.db.entity.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<Transaction>> =
        dao.getTransactionsBetween(start, end)

    suspend fun addTransaction(transaction: Transaction): Long = dao.insert(transaction)

    suspend fun updateTransaction(transaction: Transaction) = dao.update(transaction)

    suspend fun deleteTransaction(transaction: Transaction) = dao.delete(transaction)

    suspend fun deleteAll() = dao.deleteAll()
}
