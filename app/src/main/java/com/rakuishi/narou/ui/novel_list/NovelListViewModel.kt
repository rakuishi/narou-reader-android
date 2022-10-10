package com.rakuishi.narou.ui.novel_list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.repository.NovelRepository
import com.rakuishi.narou.ui.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class NovelListViewModel(
    private val novelRepository: NovelRepository,
) : ViewModel() {

    var uiState: MutableState<UiState> = mutableStateOf(UiState.Initial)
        private set
    var novelList: MutableState<List<Novel>> = mutableStateOf(arrayListOf())
        private set
    var fetchedAt: MutableState<Date?> = mutableStateOf(null)
        private set
    var snackbarMessageChannel = Channel<Int>()
        private set

    val isRefreshing: Boolean
        get() = uiState.value == UiState.Loading

    fun fetchNovelList(forceReload: Boolean = false) {
        viewModelScope.launch {
            if (forceReload || novelList.value.isEmpty()) {
                uiState.value = UiState.Loading
                novelList.value = novelRepository.fetchList(skipUpdateNewEpisode = false)
                fetchedAt.value = Date()
                uiState.value = UiState.Success
            } else {
                novelList.value = novelRepository.fetchList(skipUpdateNewEpisode = true)
                uiState.value = UiState.Success
            }
        }
    }

    fun insertNewNovel(url: String) {
        viewModelScope.launch {
            val novel: Novel? = novelRepository.insertNewNovel(url)
            if (novel != null) {
                novelList.value = novelRepository.fetchList(skipUpdateNewEpisode = true)
            } else {
                snackbarMessageChannel.send(R.string.enter_url_failed)
            }
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelRepository.delete(novel.id)
            novelList.value = novelRepository.fetchList(skipUpdateNewEpisode = true)
            val isSuccess = novelList.value.none { it.id == novel.id }
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