package com.rakuishi.nreader.ui.novel_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.rakuishi.nreader.BuildConfig
import com.rakuishi.nreader.R
import com.rakuishi.nreader.ui.component.IconDropdownMenuItem
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NovelDetailScreen(
    navController: NavController,
    viewModel: NovelDetailViewModel,
) {
    val context = LocalContext.current
    var currentUrl: String? = null
    var showMenu by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    val saveCookies = {
        currentUrl?.let {
            viewModel.saveCookies(it, CookieManager.getInstance().getCookie(it))
        }
    }
    val updateCurrentUrl: (url: String) -> Unit = {
        currentUrl = it
        viewModel.updateCurrentEpisodeNumberIfMatched(it)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.uiState.value?.novel?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                subtitle = {
                    val novel = viewModel.uiState.value?.novel ?: return@TopAppBar
                    Text(
                        text = stringResource(
                            R.string.novel_episode,
                            novel.currentEpisodeNumber,
                            novel.latestEpisodeNumber,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        saveCookies.invoke()
                        // https://github.com/google/accompanist/issues/1408
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        IconDropdownMenuItem(
                            textResId = R.string.refresh,
                            iconResId = R.drawable.ic_refresh_24
                        ) {
                            webView?.reload()
                            showMenu = false
                        }
                        IconDropdownMenuItem(
                            textResId = R.string.move_to_latest_episode,
                            iconResId = R.drawable.ic_new_episode_24
                        ) {
                            viewModel.uiState.value?.novel?.latestEpisodeUrl?.let {
                                updateCurrentUrl(it)
                                webView?.loadUrl(it)
                            }
                            showMenu = false
                        }
                        IconDropdownMenuItem(
                            textResId = R.string.open_in_chrome,
                            iconResId = R.drawable.ic_share_24
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW).setData(webView?.url?.toUri())
                            context.startActivity(intent)
                            showMenu = false
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            viewModel.uiState.value?.let { content ->
                currentUrl = content.initialUrl

                WebViewCompose(
                    content.initialUrl,
                    content.cookies,
                    { webView = it },
                    { updateCurrentUrl(it) },
                    { progress = it }
                )
            }

            if (progress != 1f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            saveCookies.invoke()
            navController.popBackStack()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewCompose(
    url: String,
    cookies: Map<String, String>,
    onCreate: (WebView) -> Unit = {},
    onChangeUrl: (url: String) -> Unit = {},
    onChangeProgress: (progress: Float) -> Unit = {},
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        request?.url?.let { onChangeUrl.invoke(it.toString()) }
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
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onChangeProgress.invoke(newProgress.toFloat() / 100f)
                    }
                }

                settings.domStorageEnabled = true
                settings.javaScriptEnabled = true

                val cookieManager = CookieManager.getInstance()
                cookies.forEach { (key, value) -> cookieManager.setCookie(key, value) }
                if (cookieManager.hasCookies()) cookieManager.flush()

                loadUrl(url)

                onCreate.invoke(this)
            }
        },
        update = {
            /* do nothing */
        }
    )
}