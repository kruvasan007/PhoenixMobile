package com.example.phoenixmobile.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.phoenixmobile.data.ChatManager
import com.example.phoenixmobile.data.ReportManager
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _triplets = MutableLiveData<List<Triplet>>(emptyList())
    val triplets: LiveData<List<Triplet>> = _triplets

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Добавляем сообщение пользователя
        val userMessage = ChatMessage(text = message, isUser = true)
        addMessage(userMessage)

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = ChatManager.sendMessage(message)

            if (result.isSuccess) {
                val chatResult = result.getOrNull()!!

                // Добавляем ответ системы
                val systemMessage = ChatMessage(text = chatResult.explanation, isUser = false)
                addMessage(systemMessage)

                // Обновляем триплеты
                _triplets.value = chatResult.triplets

            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Ошибка отправки сообщения"
                _error.value = errorMessage

                // Добавляем сообщение об ошибке
                val errorMsg = ChatMessage(text = "Ошибка: $errorMessage", isUser = false)
                addMessage(errorMsg)
            }

            _loading.value = false
        }
    }

    fun clearGraph() {
        viewModelScope.launch {
            _loading.value = true

            val result = ChatManager.clearGraph()

            if (result.isSuccess) {
                _triplets.value = emptyList()
                val clearMessage = ChatMessage(text = "Граф знаний очищен", isUser = false)
                addMessage(clearMessage)
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Ошибка очистки графа"
                _error.value = errorMessage
            }

            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Проверяем, нужно ли отправить базовый запрос
    fun checkForInitialQuery() {
        // Проверяем, что уже есть сообщения в чате
        if (_messages.value?.isNotEmpty() == true) return

        // Проверяем, был ли отправлен отчет
        if (ReportManager.hasReportBeenSent(getApplication())) {
            // Отправляем автоматический запрос
            sendMessage("Опиши мое устройство качественно на рынке")
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value ?: emptyList()
        _messages.value = currentMessages + message
    }
}
