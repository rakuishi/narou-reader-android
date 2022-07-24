package com.rakuishi.narou.ui.novel

import android.annotation.SuppressLint
import android.net.http.SslError
import android.webkit.*
import androidx.activity.compose.BackHandler
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
    var currentUrl: String? = null
    val popBackStackAndSaveCookieSettings = {
        currentUrl?.let {
            viewModel.saveCookies(it, CookieManager.getInstance().getCookie(it))
        }
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = viewModel.result.value.novel?.title ?: "",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        popBackStackAndSaveCookieSettings.invoke()
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
        if (viewModel.result.value.isSuccess) {
            val result = viewModel.result.value
            val novel = result.novel ?: throw NullPointerException()
            val cookies = result.cookies ?: throw NullPointerException()

            currentUrl = novel.currentEpisodeUrl

            WebViewCompose(novel.currentEpisodeUrl, cookies) { url ->
                currentUrl = url
                viewModel.updateCurrentEpisodeNumberIfMatched(url)
            }
        }
    }

    BackHandler {
        popBackStackAndSaveCookieSettings.invoke()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewCompose(
    url: String,
    cookies: Map<String, String>,
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
            webView.settings.javaScriptEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

            val cookieManager = CookieManager.getInstance()
            cookies.forEach { (key, value) -> cookieManager.setCookie(key, value) }
            if (cookieManager.hasCookies()) cookieManager.flush()

            webView.loadUrl(url)
        }
    )
}