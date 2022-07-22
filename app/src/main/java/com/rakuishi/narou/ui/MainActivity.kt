package com.rakuishi.narou.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rakuishi.narou.App
import com.rakuishi.narou.ui.home.HomeScreen
import com.rakuishi.narou.ui.home.HomeViewModel
import com.rakuishi.narou.ui.theme.NarouReaderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NarouReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
                }
            }
        }
    }

    @Composable
    private fun HomeScreen() {
        HomeScreen(
            viewModel = viewModel(
                factory = HomeViewModel.provideFactory((applicationContext as App).novelRepository)
            )
        )
    }
}