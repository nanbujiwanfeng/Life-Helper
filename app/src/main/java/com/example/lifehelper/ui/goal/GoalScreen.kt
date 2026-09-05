package com.example.lifehelper.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifehelper.R
import com.example.lifehelper.data.db.entity.Goal
import com.example.lifehelper.ui.common.Format
import com.example.lifehelper.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(viewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }
    var confirmDelete by remember { mutableStateOf<Goal?>(null) }

    val now = System.currentTimeMillis()

    val filtered = when (filter) {
        GoalFilter.ALL -> goals
        GoalFilter.ONGOING -> goals.filter { !it.isCompleted && !it.isExpired(now) }
        GoalFilter.DONE -> goals.filter { it.isCompleted }
        GoalFilter.EXPIRED -> goals.filter { it.isExpired(now) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.goal_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingGoal = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.goal_add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TodayFocusSection(
                goals = goals,
                now = now,
                onMarkDone = viewModel::toggleComplete
            )
            FilterRow(filter = filter, onFilter = viewModel::setFilter)

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            now = now,
                            onToggle = { viewModel.toggleComplete(goal) },
                            onEdit = {
                                editingGoal = goal
                                showDialog = true
                            },
                            onDelete = { confirmDelete = goal }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        GoalEditDialog(
            goal = editingGoal,
            onDismiss = { showDialog = false },
            onSave = { title, desc, deadline, priority, progress ->
                if (editingGoal == null) {
                    viewModel.addGoal(
                        Goal(title = title, description = desc, deadlineDate = deadline, priority = priority, progress = progress)
                    )
                } else {
                    viewModel.updateGoal(
                        editingGoal!!.copy(
                            title = title, description = desc, deadlineDate = deadline,
                            priority = priority, progress = progress
                        )
                    )
                }
                showDialog = false
            }
        )
    }

    confirmDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal)
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
private fun TodayFocusSection(
    goals: List<Goal>,
    now: Long,
    onMarkDone: (Goal) -> Unit
) {
    val twoDays = 2L * 24 * 60 * 60 * 1000
    val focusGoals = goals.filter { !it.isCompleted && it.deadlineDate != null && it.deadlineDate!! <= now + twoDays }

    if (focusGoals.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.goal_today_focus),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            focusGoals.forEach { goal ->
                val overdue = goal.deadlineDate!! < now
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (overdue) ExpenseRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (overdue) stringResource(R.string.goal_overdue)
                            else "截止 ${Format.date(goal.deadlineDate!!)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (overdue) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Button(onClick = { onMarkDone(goal) }) {
                        Text(stringResource(R.string.goal_mark_done))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(filter: GoalFilter, onFilter: (GoalFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = filter == GoalFilter.ALL, onClick = { onFilter(GoalFilter.ALL) },
            label = { Text(stringResource(R.string.goal_status_all)) })
        FilterChip(selected = filter == GoalFilter.ONGOING, onClick = { onFilter(GoalFilter.ONGOING) },
            label = { Text(stringResource(R.string.goal_status_ongoing)) })
        FilterChip(selected = filter == GoalFilter.DONE, onClick = { onFilter(GoalFilter.DONE) },
            label = { Text(stringResource(R.string.goal_status_done)) })
        FilterChip(selected = filter == GoalFilter.EXPIRED, onClick = { onFilter(GoalFilter.EXPIRED) },
            label = { Text(stringResource(R.string.goal_status_expired)) })
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    now: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val overdue = goal.isExpired(now)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else null,
                    color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface
                )
                val priorityText = when (goal.priority) {
                    3 -> stringResource(R.string.goal_priority_high)
                    2 -> stringResource(R.string.goal_priority_medium)
                    else -> stringResource(R.string.goal_priority_low)
                }
                Text(
                    text = priorityText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (goal.priority == 3) ExpenseRed else MaterialTheme.colorScheme.primary
                )
            }
            if (goal.description.isNotBlank()) {
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (goal.deadlineDate != null) {
                Text(
                    text = "截止：${Format.date(goal.deadlineDate!!)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (overdue) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onToggle) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.goal_mark_done),
                        tint = if (goal.isCompleted) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalEditDialog(
    goal: Goal?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, deadline: Long?, priority: Int, progress: Int) -> Unit
) {
    var title by remember { mutableStateOf(goal?.title ?: "") }
    var desc by remember { mutableStateOf(goal?.description ?: "") }
    var deadline by remember { mutableStateOf(goal?.deadlineDate) }
    var priority by remember { mutableStateOf(goal?.priority ?: 2) }
    var progress by remember { mutableStateOf(goal?.progress?.toFloat() ?: 0f) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (goal == null) R.string.goal_add else R.string.goal_edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.goal_field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.goal_field_desc)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text(
                        text = if (deadline == null) stringResource(R.string.goal_field_deadline)
                        else Format.date(deadline!!)
                    )
                }
                if (deadline != null) {
                    TextButton(onClick = { deadline = null }) {
                        Text(stringResource(R.string.action_cancel), color = ExpenseRed)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.goal_field_priority), style = MaterialTheme.typography.labelLarge)
                Row {
                    FilterChip(selected = priority == 1, onClick = { priority = 1 },
                        label = { Text(stringResource(R.string.goal_priority_low)) })
                    Spacer(Modifier.height(0.dp))
                    FilterChip(selected = priority == 2, onClick = { priority = 2 },
                        label = { Text(stringResource(R.string.goal_priority_medium)) },
                        modifier = Modifier.padding(horizontal = 4.dp))
                    FilterChip(selected = priority == 3, onClick = { priority = 3 },
                        label = { Text(stringResource(R.string.goal_priority_high)) })
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.goal_field_progress), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = progress,
                    onValueChange = { progress = it },
                    valueRange = 0f..100f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), desc.trim(), deadline, priority, progress.toInt())
                    }
                },
                enabled = title.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadline ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    deadline = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
