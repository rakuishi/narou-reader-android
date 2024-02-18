package com.rakuishi.nreader.ui.novel_detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.repository.DataStoreRepository
import com.rakuishi.nreader.repository.NovelRepository
import com.rakuishi.nreader.ui.Destination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NovelDetailViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val novelRepository: NovelRepository,
    private val dataStoreRepository: DataStoreRepository,
    novelId: Long,
    episodeId: String,
) : ViewModel() {

    class Content(
        val novel: Novel,
        val url: String,
        val cookies: Map<String, String>,
    )

    val content: MutableState<Content?> = mutableStateOf(null)

    init {
        viewModelScope.launch {
            val novel = novelRepository.getItemById(novelId) ?: return@launch
            val cookies: Map<String, String> = dataStoreRepository.readCookies().first()
            val currentEpisodeId = if (savedStateHandle.contains(Destination.EPISODE_ID)) {
                savedStateHandle[Destination.EPISODE_ID] ?: ""
            } else {
                episodeId
            }
            val url = novel.getEpisodeUrl(currentEpisodeId)
            updateCurrentEpisodeNumberIfMatched(url)
            delay(400L) // for smooth transition
            content.value = Content(
                novel,
                url,
                cookies
            )
        }
    }

    fun updateCurrentEpisodeNumberIfMatched(url: String) {
        viewModelScope.launch {
            val novel = novelRepository.updateCurrentEpisodeNumberIfMatched(url)
            if (novel != null) {
                savedStateHandle[Destination.EPISODE_ID] = novel.currentEpisodeId
            }
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
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return NovelDetailViewModel(
                    extras.createSavedStateHandle(),
                    novelRepository,
                    dataStoreRepository,
                    novelId,
                    episodeId,
                ) as T
            }
        }
    }
}