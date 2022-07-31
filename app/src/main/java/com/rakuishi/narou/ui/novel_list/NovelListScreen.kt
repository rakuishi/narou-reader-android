package com.rakuishi.narou.ui.novel_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.Destination
import com.rakuishi.narou.ui.UiState
import com.rakuishi.narou.ui.component.BasicDialog
import com.rakuishi.narou.ui.component.NovelListItem
import com.rakuishi.narou.ui.component.TextFieldDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelListScreen(
    navController: NavController,
    viewModel: NovelListViewModel,
) {
    val openInsertDialog = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) }
    val targetNovel: MutableState<Novel?> = remember { mutableStateOf(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = snackbarHostState) {
        viewModel.snackbarMessageChannel.receiveAsFlow().collect {
            val message = context.getString(it)
            snackbarHostState.showSnackbar(message = message)
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                openInsertDialog.value = true
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_novel)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        if (viewModel.novelList.value.isEmpty() && viewModel.uiState.value == UiState.Success) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Text(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .padding(bottom = 16.dp),
                    text = stringResource(R.string.add_novel_from_fab),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            Box {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(viewModel.isRefreshing),
                    onRefresh = { viewModel.fetchNovelList() },
                    indicator = { state, trigger ->
                        SwipeRefreshIndicator(
                            state = state,
                            refreshTriggerDistance = trigger,
                            backgroundColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                ) {
                    NovelList(
                        items = viewModel.novelList.value,
                        { novel ->
                            viewModel.consumeHasNewEpisodeIfNeeded(novel)
                            navController.navigate(Destination.createNovelRoute(novel.id))
                        },
                        { novel ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            targetNovel.value = novel
                            openDeleteDialog.value = true
                        }
                    )
                }

                viewModel.fetchedAt.value?.let {
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        text = stringResource(
                            R.string.updated_on,
                            SimpleDateFormat("MM/dd HH:mm", Locale.JAPAN).format(it)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
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