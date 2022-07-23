package com.rakuishi.narou.ui.novel

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rakuishi.narou.BuildConfig
import com.rakuishi.narou.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelScreen(
    navController: NavController,
    viewModel: NovelViewModel,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = viewModel.novel.value?.title ?: "",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
            )
        }
    ) {
        viewModel.novel.value?.let { novel ->
            WebViewCompose(url = novel.currentEpisodeUrl) { url ->
                viewModel.updateCurrentEpisodeNumberIfMatched(url)
            }
        }
    }
}

@Composable
fun WebViewCompose(
    url: String,
    onUrlChanged: (String) -> Unit,
) {
    AndroidView(
        factory = ::WebView,
        update = { webView ->
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.let { onUrlChanged.invoke(it.toString()) }
                    return super.shouldOverrideUrlLoading(view, request)
                }

                @SuppressLint("WebViewClientOnReceivedSslError")
                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?
                ) {
                    if (BuildConfig.DEBUG) {
                        // FIXME: Android Emulator has ssl error
                        handler?.proceed()
                    } else {
                        super.onReceivedSslError(view, handler, error)
                    }
                }
            }
            webView.loadUrl(url)
        }
    )
}
