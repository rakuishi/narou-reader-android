package com.rakuishi.narou.ui.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NovelRepository,
) : ViewModel() {

    var isRefreshing: MutableState<Boolean> = mutableStateOf(false)
        private set
    var novelList: MutableState<List<Novel>> = mutableStateOf(arrayListOf())
        private set

    init {
        fetchNovelList()
    }

    fun fetchNovelList() {
        viewModelScope.launch {
            isRefreshing.value = true
            novelList.value = repository.fetchList()
            isRefreshing.value = false
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