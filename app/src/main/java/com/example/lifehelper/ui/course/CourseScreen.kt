package com.example.lifehelper.ui.course

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifehelper.R
import com.example.lifehelper.data.db.entity.Course
import com.example.lifehelper.ui.theme.ExpenseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(viewModel: CourseViewModel = viewModel(factory = CourseViewModel.Factory)) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var confirmDelete by remember { mutableStateOf<Course?>(null) }
    var showConflict by remember { mutableStateOf(false) }

    val dayCourses = courses.filter { it.dayOfWeek == selectedDay }.sortedBy { it.startMinutes() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.course_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCourse = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.course_add))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            DaySelector(selectedDay = selectedDay, onSelect = viewModel::setSelectedDay)

            if (dayCourses.isEmpty()) {
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
                    items(dayCourses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            onEdit = {
                                editingCourse = course
                                showDialog = true
                            },
                            onDelete = { confirmDelete = course }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CourseEditDialog(
            course = editingCourse,
            defaultDay = selectedDay,
            onDismiss = { showDialog = false },
            onSave = { course ->
                if (editingCourse == null) {
                    viewModel.addCourse(course) { success -> showConflict = !success }
                } else {
                    viewModel.updateCourse(course) { success -> showConflict = !success }
                }
                showDialog = false
            }
        )
    }

    if (showConflict) {
        AlertDialog(
            onDismissRequest = { showConflict = false },
            title = { Text(stringResource(R.string.course_title)) },
            text = { Text(stringResource(R.string.course_conflict)) },
            confirmButton = {
                TextButton(onClick = { showConflict = false }) { Text(stringResource(R.string.action_confirm)) }
            }
        )
    }

    confirmDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCourse(course)
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
private fun DaySelector(selectedDay: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (day in 1..7) {
            val labelRes = when (day) {
                1 -> R.string.day_1
                2 -> R.string.day_2
                3 -> R.string.day_3
                4 -> R.string.day_4
                5 -> R.string.day_5
                6 -> R.string.day_6
                else -> R.string.day_7
            }
            FilterChip(
                selected = selectedDay == day,
                onClick = { onSelect(day) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${course.startTime} - ${course.endTime}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            val info = buildString {
                if (course.location.isNotBlank()) append(course.location)
                if (course.teacher.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(course.teacher)
                }
                val weekText = when (course.weekPattern) {
                    "odd" -> stringResource(R.string.course_week_odd)
                    "even" -> stringResource(R.string.course_week_even)
                    else -> stringResource(R.string.course_week_all)
                }
                append(" · ")
                append(weekText)
            }
            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

@Composable
private fun CourseEditDialog(
    course: Course?,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var location by remember { mutableStateOf(course?.location ?: "") }
    var teacher by remember { mutableStateOf(course?.teacher ?: "") }
    var day by remember { mutableStateOf(course?.dayOfWeek ?: defaultDay) }
    var startTime by remember { mutableStateOf(course?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(course?.endTime ?: "09:40") }
    var weekPattern by remember { mutableStateOf(course?.weekPattern ?: "all") }
    var remindMinutes by remember { mutableStateOf((course?.remindMinutes ?: 15).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (course == null) R.string.course_add else R.string.course_edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.course_field_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text(stringResource(R.string.course_field_location)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = teacher, onValueChange = { teacher = it },
                    label = { Text(stringResource(R.string.course_field_teacher)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime, onValueChange = { startTime = it },
                        label = { Text(stringResource(R.string.course_field_start)) },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime, onValueChange = { endTime = it },
                        label = { Text(stringResource(R.string.course_field_end)) },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.course_field_day), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (d in 1..7) {
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = {
                                Text(
                                    when (d) {
                                        1 -> stringResource(R.string.day_1)
                                        2 -> stringResource(R.string.day_2)
                                        3 -> stringResource(R.string.day_3)
                                        4 -> stringResource(R.string.day_4)
                                        5 -> stringResource(R.string.day_5)
                                        6 -> stringResource(R.string.day_6)
                                        else -> stringResource(R.string.day_7)
                                    }
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.course_field_weeks), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = weekPattern == "all", onClick = { weekPattern = "all" },
                        label = { Text(stringResource(R.string.course_week_all)) })
                    FilterChip(selected = weekPattern == "odd", onClick = { weekPattern = "odd" },
                        label = { Text(stringResource(R.string.course_week_odd)) })
                    FilterChip(selected = weekPattern == "even", onClick = { weekPattern = "even" },
                        label = { Text(stringResource(R.string.course_week_even)) })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = remindMinutes, onValueChange = { remindMinutes = it },
                    label = { Text(stringResource(R.string.course_field_remind)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                        onSave(
                            Course(
                                id = course?.id ?: 0,
                                name = name.trim(),
                                location = location.trim(),
                                teacher = teacher.trim(),
                                dayOfWeek = day,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                weekPattern = weekPattern,
                                remindMinutes = remindMinutes.toIntOrNull() ?: 0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
