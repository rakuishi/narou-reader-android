package com.rakuishi.nreader.ui.novel_list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.nreader.R
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.repository.NovelRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class NovelListViewModel(
    private val novelRepository: NovelRepository,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val error: Throwable? = null,
        val content: Content? = null
    ) {
        data class Content(
            val novelList: List<Novel>,
            val fetchedAt: Date?,
        )

        val isEmpty: Boolean
            get() = !isLoading && content?.novelList?.isEmpty() == true
    }

    var uiState: MutableState<UiState> = mutableStateOf(UiState())
        private set
    var snackbarMessageChannel = Channel<Int>()
        private set

    fun fetchNovelList(forceReload: Boolean = false) {
        viewModelScope.launch {
            val updateNewEpisode = forceReload || uiState.value.content == null
            if (updateNewEpisode) uiState.value = uiState.value.copy(isLoading = true)

            val novelList = novelRepository.fetchList(skipUpdateNewEpisode = !updateNewEpisode)
            val fetchedAt = if (updateNewEpisode) Date() else uiState.value.content?.fetchedAt
            uiState.value = uiState.value.copy(
                isLoading = false,
                error = null,
                content = UiState.Content(novelList, fetchedAt)
            )
        }
    }

    fun insertNewNovel(url: String) {
        viewModelScope.launch {
            val novel: Novel? = novelRepository.insertNewNovel(url)
            if (novel != null) {
                val novelList = novelRepository.fetchList(skipUpdateNewEpisode = true)
                uiState.value = uiState.value.copy(
                    content = uiState.value.content?.copy(
                        novelList = novelList,
                    )
                )
            } else {
                snackbarMessageChannel.send(R.string.enter_url_failed)
            }
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelRepository.delete(novel.id)

            val novelList = novelRepository.fetchList(skipUpdateNewEpisode = true)
            uiState.value = uiState.value.copy(
                content = uiState.value.content?.copy(
                    novelList = novelList,
                )
            )

            val isSuccess = novelList.none { it.id == novel.id }
            if (!isSuccess) {
                snackbarMessageChannel.send(R.string.delete_failed)
            }
        }
    }

    companion object {

        fun provideFactory(
            repository: NovelRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NovelListViewModel(repository) as T
            }
        }
    }
}