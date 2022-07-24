package com.rakuishi.narou.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.Destination
import com.rakuishi.narou.ui.component.NovelListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel,
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
            )
        },
        /*
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // TODO: Show AlertDialog with TextField
            }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_novel)
                )
            }
        }
         */
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(viewModel.isRefreshing.value),
            onRefresh = { viewModel.fetchNovelList() },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
        ) {
            NovelList(items = viewModel.novelList.value) { novel ->
                viewModel.consumeHasNewEpisodeIfNeeded(novel)
                navController.navigate(Destination.createNovelRoute(novel.id))
            }
        }
    }
}

@Composable
fun NovelList(items: List<Novel>, onNovelClicked: (novel: Novel) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items) { _, novel ->
            NovelListItem(novel = novel, onNovelClicked = { onNovelClicked.invoke(novel) })
        }
    }
}