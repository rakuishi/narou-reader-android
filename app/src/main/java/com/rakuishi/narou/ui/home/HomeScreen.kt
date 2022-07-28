package com.rakuishi.narou.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.Destination
import com.rakuishi.narou.ui.component.BasicDialog
import com.rakuishi.narou.ui.component.NovelListItem
import com.rakuishi.narou.ui.component.TextFieldDialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
) {
    val openInsertDialog = remember { mutableStateOf(false) }
    val openDeleteDialog = remember { mutableStateOf(false) }
    val targetNovel: MutableState<Novel?> = remember { mutableStateOf(null) }

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
        snackbarHost = {
            SnackbarHost(viewModel.snackbarHostState.value)
        },
    ) {
        Box {
            SwipeRefresh(
                state = rememberSwipeRefreshState(viewModel.isRefreshing.value),
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