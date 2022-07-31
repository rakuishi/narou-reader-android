package com.rakuishi.narou.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.R
import com.rakuishi.narou.repository.NovelRepository
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.channels.Channel
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
    var snackbarMessageChannel = Channel<Int>()
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
                novelList.value = novelRepository.fetchList(skipUpdateNewEpisode = true)
            } else {
                snackbarMessageChannel.send(R.string.insert_failed)
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