package com.example.lifehelper.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifehelper.LifeHelperApp
import com.example.lifehelper.data.db.entity.Transaction
import com.example.lifehelper.data.repository.ProfileRepository
import com.example.lifehelper.data.repository.TransactionRepository
import com.example.lifehelper.ui.common.Format
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /** 当前查看的月份（该月第一天 0 点） */
    private val _month = MutableStateFlow(Format.monthStart())
    val month: StateFlow<Long> = _month.asStateFlow()

    /** 当月账目 */
    val transactions: StateFlow<List<Transaction>> = _month
        .flatMapLatest { start ->
            repository.getTransactionsBetween(start, Format.nextMonthStart(start))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 月预算（0 表示未设置） */
    private val _budget = MutableStateFlow(profileRepository.getMonthlyBudget())
    val budget: StateFlow<Double> = _budget.asStateFlow()

    fun previousMonth() = _month.value.let { start ->
        val c = java.util.Calendar.getInstance().apply { timeInMillis = start; add(java.util.Calendar.MONTH, -1) }
        _month.value = Format.monthStart(c.timeInMillis)
    }

    fun nextMonth() = _month.value.let { start ->
        val c = java.util.Calendar.getInstance().apply { timeInMillis = start; add(java.util.Calendar.MONTH, 1) }
        _month.value = Format.monthStart(c.timeInMillis)
    }

    fun setBudget(value: Double) {
        profileRepository.setMonthlyBudget(value)
        _budget.value = value
    }

    fun addTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.addTransaction(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.updateTransaction(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LifeHelperApp
                TransactionViewModel(app.container.transactionRepository, app.container.profileRepository)
            }
        }
    }
}
