package com.rakuishi.narou.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel(
    private val novelRepository: NovelRepository,
) : ViewModel() {

    var isRefreshing: MutableState<Boolean> = mutableStateOf(false)
        private set
    var novelList: MutableState<List<Novel>> = mutableStateOf(arrayListOf())
        private set
    var fetchedAt: MutableState<Date?> = mutableStateOf(null)
        private set
    var snackbarHostState: MutableState<SnackbarHostState> = mutableStateOf(SnackbarHostState())
        private set

    init {
        fetchNovelList()
    }

    fun fetchNovelList(skipUpdateNewEpisode: Boolean = false) {
        viewModelScope.launch {
            isRefreshing.value = true
            novelList.value = novelRepository.fetchList(skipUpdateNewEpisode)
            fetchedAt.value = Date()
            isRefreshing.value = false
        }
    }

    fun insertNewNovel(url: String) {
        viewModelScope.launch {
            val novel: Novel? = novelRepository.insertNewNovel(url)
            if (novel != null) {
                fetchNovelList(skipUpdateNewEpisode = true)
            } else {
                snackbarHostState.value.showSnackbar(message = "The URL doesn\\'t match https://ncode.syosetu.com/***/")
            }
        }
    }

    fun deleteNovel(novel: Novel) {
        viewModelScope.launch {
            novelRepository.delete(novel.id)
            fetchNovelList(skipUpdateNewEpisode = true)
            val isSuccess = novelList.value.none { it.id == novel.id }
            if (!isSuccess) {
                snackbarHostState.value.showSnackbar(message = "Failed to delete the novel")
            }
        }
    }

    fun consumeHasNewEpisodeIfNeeded(novel: Novel) {
        viewModelScope.launch {
            novelRepository.consumeHasNewEpisodeIfNeeded(novel)
        }
    }

    companion object {

        fun provideFactory(
            repository: NovelRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repository) as T
            }
        }
    }
}