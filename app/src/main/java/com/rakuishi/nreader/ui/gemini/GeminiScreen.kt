package com.rakuishi.nreader.ui.gemini

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GeminiScreen(viewModel: GeminiViewModel) {
    val uiState by viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.generateContext()
                }

                else -> {
                    /* do nothing */
                }
            }
        }
        lifecycleOwner.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.removeObserver(lifecycleObserver)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        if (uiState.isLoading) {
            CircularWavyProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center),
            )
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = uiState.generateText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
} 