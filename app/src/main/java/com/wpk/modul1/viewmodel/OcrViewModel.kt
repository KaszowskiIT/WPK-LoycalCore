package com.wpk.modul1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OcrViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    data class ChatUiState(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class ChatMessage(
        val content: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Add user message
        val userMessage = ChatMessage(
            content = message,
            isUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            errorMessage = null
        )

        // Simulate AI response
        viewModelScope.launch {
            try {
                delay(1000) // Simulate processing time

                val aiResponse = ChatMessage(
                    content = generateResponse(message),
                    isUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiResponse,
                    isLoading = false
                )

            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send message: ${exception.message}"
                )
            }
        }
    }

    private fun generateResponse(userMessage: String): String {
        return when {
            userMessage.contains("ocr", ignoreCase = true) ||
                    userMessage.contains("scan", ignoreCase = true) -> {
                "I can help you with OCR text recognition. Use the camera button to scan text from images."
            }
            userMessage.contains("camera", ignoreCase = true) -> {
                "Tap the camera icon in the top bar to start scanning text from your camera."
            }
            userMessage.contains("hello", ignoreCase = true) ||
                    userMessage.contains("hi", ignoreCase = true) -> {
                "Hello! I'm your OCR assistant. I can help you extract and analyze text from images."
            }
            else -> {
                "I understand you said: \"$userMessage\". How can I help you with text recognition or analysis?"
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            errorMessage = null
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
