package com.example.lifehelper.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifehelper.LifeHelperApp
import com.example.lifehelper.data.repository.ProfileRepository
import com.example.lifehelper.util.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _nickname = MutableStateFlow(profileRepository.getNickname())
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    private val _signature = MutableStateFlow(profileRepository.getSignature())
    val signature: StateFlow<String> = _signature.asStateFlow()

    private val _avatar = MutableStateFlow(profileRepository.getAvatar())
    val avatar: StateFlow<String> = _avatar.asStateFlow()

    private val _themeMode = MutableStateFlow(profileRepository.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(profileRepository.getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _notificationEnabled = MutableStateFlow(profileRepository.isNotificationEnabled())
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setNickname(value: String) {
        _nickname.value = value
        profileRepository.setNickname(value)
    }

    fun setSignature(value: String) {
        _signature.value = value
        profileRepository.setSignature(value)
    }

    fun setAvatar(value: String) {
        _avatar.value = value
        profileRepository.setAvatar(value)
    }

    fun setThemeMode(value: String) {
        _themeMode.value = value
        profileRepository.setThemeMode(value)
    }

    fun setLanguage(value: String) {
        _language.value = value
        profileRepository.setLanguage(value)
    }

    fun setNotificationEnabled(value: Boolean) {
        _notificationEnabled.value = value
        profileRepository.setNotificationEnabled(value)
    }

    fun showMessage(text: String) {
        _message.value = text
    }

    fun clearMessage() {
        _message.value = null
    }

    fun exportData(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        onDone(backupManager.export(uri))
    }

    fun importData(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        onDone(backupManager.import(uri))
    }

    fun exportCsv(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        onDone(backupManager.exportTransactionsCsv(uri))
    }

    /** 清除所有数据（数据库 + 本地设置），随后由界面重建 */
    fun clearAllData(onDone: () -> Unit) = viewModelScope.launch {
        backupManager.clearAllData()
        profileRepository.clearAll()
        onDone()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LifeHelperApp
                ProfileViewModel(
                    app.container.profileRepository,
                    BackupManager(app)
                )
            }
        }
    }
}
