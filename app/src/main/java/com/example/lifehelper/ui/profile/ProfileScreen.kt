package com.example.lifehelper.ui.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifehelper.R
import com.example.lifehelper.ui.theme.ExpenseRed
import com.example.lifehelper.util.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onThemeChanged: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val context = LocalContext.current
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val signature by viewModel.signature.collectAsStateWithLifecycle()
    val avatar by viewModel.avatar.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val notificationEnabled by viewModel.notificationEnabled.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    var showEditProfile by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    // 头像选择（从相册）
    val avatarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val base64 = ImageUtils.uriToBase64(context, it)
            if (base64 != null) viewModel.setAvatar(base64)
        }
    }

    // 备份导出 / 导入 / CSV
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportData(it) { ok ->
                viewModel.showMessage(
                    context.getString(if (ok) R.string.backup_success else R.string.backup_failed)
                )
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importData(it) { ok ->
                viewModel.showMessage(
                    context.getString(if (ok) R.string.restore_success else R.string.restore_failed)
                )
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            viewModel.exportCsv(it) { ok ->
                viewModel.showMessage(
                    context.getString(if (ok) R.string.backup_success else R.string.backup_failed)
                )
            }
        }
    }

    // 提示信息
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.profile_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 用户信息卡
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(avatar = avatar, onClick = { avatarLauncher.launch("image/*") })
                    Spacer(Modifier.size(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nickname.ifBlank { stringResource(R.string.app_name) },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = signature.ifBlank { stringResource(R.string.profile_signature) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.profile_edit_profile),
                        modifier = Modifier
                            .clickable { showEditProfile = true }
                            .padding(8.dp)
                    )
                }
            }

            // 设置卡
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.profile_settings), style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.profile_notification))
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = viewModel::setNotificationEnabled
                        )
                    }

                    // 主题选择
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.profile_theme), modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = themeMode == "light",
                                onClick = {
                                    viewModel.setThemeMode("light")
                                    onThemeChanged("light")
                                },
                                label = { Text(stringResource(R.string.profile_theme_light)) }
                            )
                            FilterChip(
                                selected = themeMode == "dark",
                                onClick = {
                                    viewModel.setThemeMode("dark")
                                    onThemeChanged("dark")
                                },
                                label = { Text(stringResource(R.string.profile_theme_dark)) }
                            )
                            FilterChip(
                                selected = themeMode == "system",
                                onClick = {
                                    viewModel.setThemeMode("system")
                                    onThemeChanged("system")
                                },
                                label = { Text(stringResource(R.string.profile_theme_system)) }
                            )
                        }
                    }

                    // 语言选择
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.profile_language), modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = language == "zh",
                                onClick = {
                                    viewModel.setLanguage("zh")
                                    context.findActivity()?.recreate()
                                },
                                label = { Text(stringResource(R.string.profile_language_zh)) }
                            )
                            FilterChip(
                                selected = language == "en",
                                onClick = {
                                    viewModel.setLanguage("en")
                                    context.findActivity()?.recreate()
                                },
                                label = { Text(stringResource(R.string.profile_language_en)) }
                            )
                            FilterChip(
                                selected = language == "system",
                                onClick = {
                                    viewModel.setLanguage("system")
                                    context.findActivity()?.recreate()
                                },
                                label = { Text(stringResource(R.string.profile_theme_system)) }
                            )
                        }
                    }
                }
            }

            // 数据备份与恢复卡
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.profile_backup), style = MaterialTheme.typography.titleMedium)
                    ActionRow(
                        label = stringResource(R.string.action_export),
                        onClick = { exportLauncher.launch("lifehelper_backup.json") }
                    )
                    ActionRow(
                        label = stringResource(R.string.action_import),
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                    ActionRow(
                        label = stringResource(R.string.action_export_csv),
                        onClick = { csvLauncher.launch("lifehelper_transactions.csv") }
                    )
                }
            }

            // 关于卡
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.profile_about), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${stringResource(R.string.profile_version)}：${stringResource(R.string.profile_version_value)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.profile_license),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(R.string.profile_repo_url),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 清除所有数据
            TextButton(
                onClick = { confirmClear = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_clear_all), color = ExpenseRed)
            }
        }
    }

    if (showEditProfile) {
        EditProfileDialog(
            nickname = nickname,
            signature = signature,
            onDismiss = { showEditProfile = false },
            onSave = { n, s ->
                viewModel.setNickname(n)
                viewModel.setSignature(s)
                showEditProfile = false
            }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.confirm_clear_all_title)) },
            text = { Text(stringResource(R.string.confirm_clear_all_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData {
                        confirmClear = false
                        context.findActivity()?.recreate()
                    }
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}

@Composable
private fun Avatar(avatar: String, onClick: () -> Unit) {
    val bitmap: Bitmap? = remember(avatar) { ImageUtils.base64ToBitmap(avatar) }
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "我",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun EditProfileDialog(
    nickname: String,
    signature: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var n by remember { mutableStateOf(nickname) }
    var s by remember { mutableStateOf(signature) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit_profile)) },
        text = {
            Column {
                OutlinedTextField(
                    value = n, onValueChange = { n = it },
                    label = { Text(stringResource(R.string.profile_nickname)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = s, onValueChange = { s = it },
                    label = { Text(stringResource(R.string.profile_signature)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(n.trim(), s.trim()) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

/** 从 Context 中查找宿主 Activity */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
