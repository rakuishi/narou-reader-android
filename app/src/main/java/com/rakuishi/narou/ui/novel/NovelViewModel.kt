package com.rakuishi.narou.ui.novel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rakuishi.narou.data.DataStoreRepository
import com.rakuishi.narou.data.NovelRepository
import com.rakuishi.narou.model.Novel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NovelViewModel(
    private val novelRepository: NovelRepository,
    private val dataStoreRepository: DataStoreRepository,
    novelId: Long,
) : ViewModel() {

    class Result(
        var isSuccess: Boolean = true,
        val novel: Novel? = null,
        val cookies: Map<String, String>? = null
    )

    var result: MutableState<Result> = mutableStateOf(Result(isSuccess = false))
        private set

    init {
        viewModelScope.launch {
            val novel = novelRepository.getItemById(novelId) ?: return@launch
            val cookies: Map<String, String> = dataStoreRepository.readCookies().first()
            delay(400L) // for smooth transition
            result.value = Result(isSuccess = true, novel, cookies)
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
                return NovelViewModel(novelRepository, dataStoreRepository, novelId) as T
            }
        }
    }
}