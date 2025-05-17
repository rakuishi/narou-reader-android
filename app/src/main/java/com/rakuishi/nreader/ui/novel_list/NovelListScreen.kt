package com.rakuishi.nreader.ui.novel_list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rakuishi.nreader.R
import com.rakuishi.nreader.model.Novel
import com.rakuishi.nreader.ui.Destination
import com.rakuishi.nreader.ui.component.BasicDialog
import com.rakuishi.nreader.ui.component.NovelListItem
import com.rakuishi.nreader.ui.component.TextFieldDialog
import com.rakuishi.nreader.util.LocaleUtil
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NovelListScreen(
    navController: NavController,
    viewModel: NovelListViewModel,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current.lifecycle
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val openInsertDialog = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) }
    val targetNovel: MutableState<Novel?> = remember { mutableStateOf(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ -> /* do nothing */ }
    )

    val refreshScope = rememberCoroutineScope()
    fun refresh() = refreshScope.launch {
        viewModel.fetchNovelList(forceReload = true)
    }

    val pullToRefreshState = rememberPullToRefreshState()

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = Manifest.permission.POST_NOTIFICATIONS
                        val state = ContextCompat.checkSelfPermission(context, permission)
                        if (state != PackageManager.PERMISSION_GRANTED) {
                            permissionLauncher.launch(permission)
                        }
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    viewModel.fetchNovelList()
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

    LaunchedEffect(snackbarHostState) {
        viewModel.snackbarMessageChannel.receiveAsFlow().collect {
            val message =
                if (it.stringResId != null) context.getString(it.stringResId)
                else it.string
            message?.let { snackbarHostState.showSnackbar(message = it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                subtitle = {
                    viewModel.uiState.value.content?.fetchedAt?.let {
                        Text(
                            text = stringResource(
                                R.string.updated_on,
                                SimpleDateFormat(
                                    if (LocaleUtil.isJa()) "M月d日 HH:mm" else "HH:mm, d LLLL",
                                    if (LocaleUtil.isJa()) Locale.JAPAN else Locale.US
                                ).format(it)
                            ),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        openInsertDialog.value = true
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add_novel)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = viewModel.uiState.value.isLoading,
            onRefresh = ::refresh,
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (viewModel.uiState.value.isEmpty) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .padding(all = 16.dp),
                    text = stringResource(R.string.add_novel_from_fab),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(padding)
            ) {
                NovelList(
                    items = viewModel.uiState.value.content?.novelList ?: emptyList(),
                    { novel ->
                        navController.navigate(
                            Destination.createNovelDetailRoute(novel)
                        )
                    },
                    { novel ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        targetNovel.value = novel
                        openDeleteDialog.value = true
                    }
                )

                PullToRefreshDefaults.LoadingIndicator(
                    isRefreshing = viewModel.uiState.value.isLoading,
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    if (openInsertDialog.value) {
        TextFieldDialog(
            title = R.string.enter_url_title,
            placeholder = R.string.enter_url_placeholder,
            openDialog = openInsertDialog,
            onPositiveClick = { viewModel.insertNewNovel(it) }
        )
    }

    if (openDeleteDialog.value) {
        BasicDialog(
            title = R.string.delete_dialog_title,
            message = R.string.delete_dialog_message,
            openDialog = openDeleteDialog,
            onPositiveText = R.string.delete,
            onPositiveClick = {
                targetNovel.value?.let { viewModel.deleteNovel(it) }
            }
        )
    }
}

@Composable
fun NovelList(
    items: List<Novel>,
    onClickNovel: (novel: Novel) -> Unit,
    onLongClickNovel: (novel: Novel) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items) { _, novel ->
            NovelListItem(
                novel,
                onClickNovel,
                onLongClickNovel,
            )
        }
    }
}