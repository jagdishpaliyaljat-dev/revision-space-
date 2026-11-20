package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.chat.StudyChatPrompt
import com.example.data.chat.StudyTurn
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeBackend
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "StudyChatViewModel"

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)

class StudyChatViewModel : ViewModel() {

    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.8-flash")
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var nextId = 0L

    fun send(userText: String, studyContext: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isSending.value) return

        _messages.update { it + ChatMessage(nextId++, trimmed, isUser = true) }
        _isSending.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val prompt = StudyChatPrompt.build(
                    studyContext = studyContext,
                    turns = _messages.value.map { msg -> StudyTurn(msg.isUser, msg.text) }
                )
                val response = withTimeout(60_000L) { model.generateContent(prompt) }
                val text = response.text
                if (text.isNullOrBlank()) {
                    _error.value = "AI returned an empty response. Please try again."
                } else {
                    _messages.update { it + ChatMessage(nextId++, text.trim(), isUser = false) }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat request timed out", e)
                _error.value = "The answer took too long. Please try again."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat request failed", e)
                _error.value = AiErrorMessages.forThrowable(e)
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearError() { _error.value = null }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }
}
