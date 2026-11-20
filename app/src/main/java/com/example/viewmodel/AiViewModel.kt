package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiViewModel"

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

class AiViewModel : ViewModel() {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _summaryState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val summaryState: StateFlow<AiUiState> = _summaryState.asStateFlow()

    fun generateSummary(topicName: String, notes: String) {
        if (notes.isBlank()) {
            _summaryState.value = AiUiState.Error("No notes available to summarize.")
            return
        }

        viewModelScope.launch {
            _summaryState.value = AiUiState.Loading
            try {
                val prompt = """
                    You are a study assistant. Please provide a concise, high-impact summary of the following notes for the topic "$topicName".
                    Focus on key concepts, formulas, or facts that are essential for revision.
                    
                    Notes:
                    $notes
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    model.generateContent(prompt)
                }
                
                val text = response.text
                if (!text.isNullOrBlank()) {
                    _summaryState.value = AiUiState.Success(text)
                } else {
                    _summaryState.value = AiUiState.Error("Model returned an empty response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Generation timed out", e)
                _summaryState.value = AiUiState.Error("Summary generation timed out. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _summaryState.value = AiUiState.Error(e.localizedMessage ?: "Failed to generate summary.")
            }
        }
    }
    
    fun resetState() {
        _summaryState.value = AiUiState.Idle
    }
}
