package com.rakuishi.narou.ui.novel_detail

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.rakuishi.narou.BuildConfig
import com.rakuishi.narou.R
import com.rakuishi.narou.ui.UiState
import com.rakuishi.narou.ui.component.IconDropdownMenuItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelDetailScreen(
    navController: NavController,
    viewModel: NovelDetailViewModel,
) {
    val context = LocalContext.current
    var currentUrl: String? = null
    var showMenu by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableStateOf(0f) }
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
                        text = viewModel.content.value.novel?.title ?: "",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        saveCookies.invoke()
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
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
                            textResId = R.string.move_to_latest_episode,
                            iconResId = R.drawable.ic_new_episode_24
                        ) {
                            viewModel.content.value.novel?.latestEpisodeUrl?.let {
                                updateCurrentUrl(it)
                                webView?.loadUrl(it)
                            }
                            showMenu = false
                        }
                        IconDropdownMenuItem(
                            textResId = R.string.open_in_chrome,
                            iconResId = R.drawable.ic_share_24
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(currentUrl))
                            context.startActivity(intent)
                            showMenu = false
                        }
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier.padding(padding)
        ) {
            if (viewModel.uiState.value == UiState.Success) {
                val content = viewModel.content.value
                val url = content.url ?: throw NullPointerException()
                val cookies = content.cookies ?: throw NullPointerException()

                currentUrl = url

                WebViewCompose(
                    url,
                    cookies,
                    { webView = it },
                    { updateCurrentUrl(it) },
                    { progress = it }
                )
            }

            if (progress != 1f) {
                LinearProgressIndicator(
                    progress = progress,
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