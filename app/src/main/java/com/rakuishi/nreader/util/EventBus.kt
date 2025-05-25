package com.rakuishi.nreader.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object EventBus {

    private val flow = MutableSharedFlow<String>()

    suspend fun publish(event: String) {
        flow.emit(event)
    }

    fun subscribe(scope: CoroutineScope, onSubscribe: (String) -> Unit) {
        flow.onEach { onSubscribe(it) }
            .launchIn(scope)
    }
}