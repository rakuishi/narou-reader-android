package com.rakuishi.nreader.ui.novel_detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.repository.DataStoreRepository
import com.rakuishi.nreader.repository.NovelRepository
import com.rakuishi.nreader.ui.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NovelDetailViewModel(
    private val novelRepository: NovelRepository,
    private val dataStoreRepository: DataStoreRepository,
    novelId: Long,
    episodeId: String,
) : ViewModel() {

    class Content(
        val novel: Novel? = null,
        val url: String? = null,
        val cookies: Map<String, String>? = null
    )

    var uiState: MutableState<UiState> = mutableStateOf(UiState.Initial)
        private set
    var content: MutableState<Content> = mutableStateOf(Content())
        private set

    init {
        viewModelScope.launch {
            val novel = novelRepository.getItemById(novelId) ?: return@launch
            val cookies: Map<String, String> = dataStoreRepository.readCookies().first()
            val url = novel.getEpisodeUrl(episodeId)
            updateCurrentEpisodeNumberIfMatched(url)
            delay(400L) // for smooth transition
            content.value = Content(
                novel,
                url,
                cookies
            )
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
            episodeId: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NovelDetailViewModel(
                    novelRepository,
                    dataStoreRepository,
                    novelId,
                    episodeId,
                ) as T
            }
        }
    }
}