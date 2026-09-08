package com.example.lifehelper.ui.transaction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifehelper.R
import com.example.lifehelper.data.db.entity.Transaction
import com.example.lifehelper.ui.common.Format
import com.example.lifehelper.ui.theme.ExpenseRed
import com.example.lifehelper.ui.theme.IncomeGreen

private val chartColors = listOf(
    Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFF9A825),
    Color(0xFF7B1FA2), Color(0xFFD32F2F), Color(0xFF00838F), Color(0xFFEF6C00)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val month by viewModel.month.collectAsStateWithLifecycle()
    val budget by viewModel.budget.collectAsStateWithLifecycle()
    val customExpense by viewModel.customExpense.collectAsStateWithLifecycle()
    val customIncome by viewModel.customIncome.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Transaction?>(null) }
    var confirmDelete by remember { mutableStateOf<Transaction?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val totalIncome = transactions.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == Transaction.TYPE_EXPENSE }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    // 支出分类统计
    val categoryStats = transactions
        .filter { it.type == Transaction.TYPE_EXPENSE }
        .groupBy { it.category }
        .map { (k, v) -> k to v.sumOf { it.amount } }
        .sortedByDescending { it.second }

    // 搜索过滤：按分类或备注匹配（忽略大小写）
    val filteredTransactions = if (searchQuery.isBlank()) {
        transactions
    } else {
        transactions.filter {
            it.category.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.transaction_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.transaction_add))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { MonthSelector(month = month, onPrev = viewModel::previousMonth, onNext = viewModel::nextMonth) }

            item {
                SummaryCard(
                    income = totalIncome,
                    expense = totalExpense,
                    balance = balance,
                    budget = budget,
                    onBudgetClick = { showBudgetDialog = true }
                )
            }

            if (categoryStats.isNotEmpty()) {
                item {
                    CategoryCard(categoryStats = categoryStats)
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.transaction_search)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_cancel))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.empty_hint),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else if (filteredTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.transaction_search_empty),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { t ->
                    TransactionRow(
                        transaction = t,
                        onEdit = { editing = t; showDialog = true },
                        onDelete = { confirmDelete = t }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TransactionEditDialog(
            transaction = editing,
            customExpense = customExpense,
            customIncome = customIncome,
            onAddCategory = viewModel::addCategory,
            onDismiss = { showDialog = false },
            onSave = { t ->
                if (editing == null) viewModel.addTransaction(t) else viewModel.updateTransaction(t)
                showDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            current = budget,
            onDismiss = { showBudgetDialog = false },
            onSave = { viewModel.setBudget(it) }
        )
    }

    confirmDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(t)
                    confirmDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun MonthSelector(month: Long, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "上一月")
        }
        Text(
            text = Format.month(month),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "下一月")
        }
    }
}

@Composable
private fun SummaryCard(
    income: Double,
    expense: Double,
    balance: Double,
    budget: Double,
    onBudgetClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.transaction_month_summary),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.transaction_income), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("¥$income", color = IncomeGreen, style = MaterialTheme.typography.titleMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.transaction_expense), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("¥$expense", color = ExpenseRed, style = MaterialTheme.typography.titleMedium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("结余", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("¥$balance", style = MaterialTheme.typography.titleMedium)
            }

            if (budget > 0) {
                Spacer(Modifier.height(12.dp))
                val ratio = (expense / budget).coerceIn(0.0, 1.0).toFloat()
                Text(
                    text = "${stringResource(R.string.transaction_budget)}：¥$expense / ¥$budget",
                    style = MaterialTheme.typography.bodyMedium
                )
                LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                if (expense > budget) {
                    Text(
                        text = stringResource(R.string.transaction_over_budget),
                        color = ExpenseRed,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            TextButton(onClick = onBudgetClick) {
                Text(stringResource(R.string.transaction_budget))
            }
        }
    }
}

@Composable
private fun CategoryCard(categoryStats: List<Pair<String, Double>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("分类统计", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryPieChart(categoryStats, modifier = Modifier.size(120.dp))
                Spacer(Modifier.size(16.dp))
                Column {
                    categoryStats.forEachIndexed { index, (name, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        chartColors[index % chartColors.size],
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = "$name  ¥$value",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPieChart(categoryStats: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val total = categoryStats.sumOf { it.second }
    Canvas(modifier = modifier) {
        if (total <= 0) return@Canvas
        var startAngle = -90f
        categoryStats.forEachIndexed { index, (_, value) ->
            val sweep = (value / total * 360).toFloat()
            drawArc(
                color = chartColors[index % chartColors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == Transaction.TYPE_INCOME
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = Format.date(transaction.date) +
                        if (transaction.note.isNotBlank()) " · ${transaction.note}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = (if (isIncome) "+" else "-") + "¥${transaction.amount}",
                color = if (isIncome) IncomeGreen else ExpenseRed,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionEditDialog(
    transaction: Transaction?,
    customExpense: List<String>,
    customIncome: List<String>,
    onAddCategory: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: Transaction.TYPE_EXPENSE) }
    var amount by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: TransactionCategories.expense.first()) }
    var date by remember { mutableStateOf(transaction?.date ?: System.currentTimeMillis()) }
    var note by remember { mutableStateOf(transaction?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val custom = if (type == Transaction.TYPE_INCOME) customIncome else customExpense
    val categories = TransactionCategories.categoriesFor(type) + custom

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (transaction == null) R.string.transaction_add else R.string.transaction_edit)) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == Transaction.TYPE_EXPENSE,
                        onClick = {
                            type = Transaction.TYPE_EXPENSE
                            category = TransactionCategories.expense.first()
                        },
                        label = { Text(stringResource(R.string.transaction_expense)) }
                    )
                    FilterChip(
                        selected = type == Transaction.TYPE_INCOME,
                        onClick = {
                            type = Transaction.TYPE_INCOME
                            category = TransactionCategories.income.first()
                        },
                        label = { Text(stringResource(R.string.transaction_income)) }
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.transaction_amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.transaction_category), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c) }
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showAddCategory = true },
                        label = { Text("+") }
                    )
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.transaction_date) + ": " + Format.date(date))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.transaction_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (value != null && value > 0) {
                        onSave(
                            Transaction(
                                id = transaction?.id ?: 0,
                                type = type,
                                amount = value,
                                category = category,
                                date = date,
                                note = note.trim()
                            )
                        )
                    }
                },
                enabled = amount.toDoubleOrNull() != null
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text(stringResource(R.string.transaction_add_category)) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.transaction_category_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newCategoryName.trim()
                        if (name.isNotEmpty()) {
                            onAddCategory(type, name)
                            category = name
                        }
                        newCategoryName = ""
                        showAddCategory = false
                    },
                    enabled = newCategoryName.isNotBlank()
                ) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = state.selectedDateMillis ?: date
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun BudgetDialog(
    current: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var input by remember { mutableStateOf(if (current > 0) current.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transaction_budget)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.transaction_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                input.toDoubleOrNull()?.let { onSave(it) }
                onDismiss()
            }) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
