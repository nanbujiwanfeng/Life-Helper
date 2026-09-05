package com.example.lifehelper.ui.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.lifehelper.LifeHelperApp
import com.example.lifehelper.data.db.entity.TranslationRecord
import com.example.lifehelper.data.repository.TranslationRepository
import com.example.lifehelper.network.TranslationApi
import com.example.lifehelper.network.TranslationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TranslateViewModel(
    private val repository: TranslationRepository,
    private val api: TranslationApi
) : ViewModel() {

    val history: StateFlow<List<TranslationRecord>> = repository.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _sourceLang = MutableStateFlow("zh-CN")
    val sourceLang: StateFlow<String> = _sourceLang.asStateFlow()

    private val _targetLang = MutableStateFlow("en")
    val targetLang: StateFlow<String> = _targetLang.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _outputText = MutableStateFlow("")
    val outputText: StateFlow<String> = _outputText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setSourceLang(code: String) {
        _sourceLang.value = code
    }

    fun setTargetLang(code: String) {
        _targetLang.value = code
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun swapLanguages() {
        val oldSource = _sourceLang.value
        val oldTarget = _targetLang.value
        // 自动检测不能作为目标语言，交换时若源为 auto 则跳过交换源语言
        if (oldSource != "auto" && oldTarget != "auto") {
            _sourceLang.value = oldTarget
            _targetLang.value = oldSource
            val oldInput = _inputText.value
            val oldOutput = _outputText.value
            _inputText.value = oldOutput
            _outputText.value = oldInput
        }
    }

    fun translate() = viewModelScope.launch {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return@launch

        _isTranslating.value = true
        _error.value = null
        try {
            if (!TranslationService.isConfigured) {
                _error.value = "未配置 DeepSeek API 密钥（见 secrets.properties）"
                return@launch
            }
            val response = api.chat(
                TranslationService.buildRequest(_sourceLang.value, _targetLang.value, text)
            )
            val result = response.choices?.firstOrNull()?.message?.content?.trim()
            if (!result.isNullOrBlank()) {
                _outputText.value = result
                repository.addRecord(
                    TranslationRecord(
                        sourceText = text,
                        translatedText = result,
                        sourceLang = _sourceLang.value,
                        targetLang = _targetLang.value
                    )
                )
            } else {
                _error.value = "翻译失败，请稍后重试"
            }
        } catch (e: Exception) {
            _error.value = "翻译失败，请检查网络或 API 密钥是否正确"
        } finally {
            _isTranslating.value = false
        }
    }

    fun deleteRecord(record: TranslationRecord) = viewModelScope.launch {
        repository.deleteRecord(record)
    }

    fun clearHistory() = viewModelScope.launch {
        repository.deleteAll()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as LifeHelperApp
                TranslateViewModel(
                    app.container.translationRepository,
                    TranslationService.api
                )
            }
        }
    }
}
