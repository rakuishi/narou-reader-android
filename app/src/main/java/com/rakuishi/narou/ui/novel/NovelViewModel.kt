package com.rakuishi.narou.ui.novel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.launch

class NovelViewModel(
    private val repository: NovelRepository,
    novelId: Int,
) : ViewModel() {

    var novel: MutableState<Novel?> = mutableStateOf(null)
        private set

    init {
        fetchNovel(novelId)
    }

    private fun fetchNovel(id: Int) {
        viewModelScope.launch {
            repository.getItemById(id)?.let {
                novel.value = it
            }
        }
    }

    companion object {

        fun provideFactory(
            repository: NovelRepository,
            novelId: Int,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NovelViewModel(repository, novelId) as T
            }
        }
    }
}