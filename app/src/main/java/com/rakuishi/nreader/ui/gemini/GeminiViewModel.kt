package com.rakuishi.nreader.ui.gemini

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch

class GeminiViewModel(
    private val inputText: String,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val generateText: String = ""
    )

    var uiState: MutableState<UiState> = mutableStateOf(UiState())
        private set

    fun generateContext() {
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")

        viewModelScope.launch {
            val prompt = """
                以下の単語について、振り仮名付きで解説お願いします。
                単語: $inputText
            """.trimIndent()
            val result = model.generateContent(prompt).text ?: ""
            uiState.value = uiState.value.copy(
                isLoading = false,
                generateText = result
            )
        }
    }
} 