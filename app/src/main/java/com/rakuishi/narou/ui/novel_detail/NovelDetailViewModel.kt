package com.rakuishi.narou.ui.novel_detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.repository.DataStoreRepository
import com.rakuishi.narou.repository.NovelRepository
import com.rakuishi.narou.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NovelDetailViewModel(
    private val novelRepository: NovelRepository,
    private val dataStoreRepository: DataStoreRepository,
    novelId: Long,
) : ViewModel() {

    class Result(
        val novel: Novel? = null,
        val cookies: Map<String, String>? = null
    )

    var uiState: MutableState<UiState> = mutableStateOf(UiState.Initial)
        private set
    var result: MutableState<Result> = mutableStateOf(Result())
        private set

    init {
        viewModelScope.launch {
            val novel = novelRepository.getItemById(novelId) ?: return@launch
            val cookies: Map<String, String> = dataStoreRepository.readCookies().first()
            delay(400L) // for smooth transition
            result.value = Result(novel, cookies)
            uiState.value = UiState.Success
        }
    }

    fun updateCurrentEpisodeNumberIfMatched(url: String) {
        viewModelScope.launch {
            novelRepository.updateCurrentEpisodeNumberIfMatched(url)
        }
    }

    fun saveCookies(url: String, cookiesString: String) {
        viewModelScope.launch {
            dataStoreRepository.saveCookies(url, cookiesString)
        }
    }

    companion object {

        fun provideFactory(
            novelRepository: NovelRepository,
            dataStoreRepository: DataStoreRepository,
            novelId: Long,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NovelDetailViewModel(novelRepository, dataStoreRepository, novelId) as T
            }
        }
    }
}