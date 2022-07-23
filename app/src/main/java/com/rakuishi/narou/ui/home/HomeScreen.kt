package com.rakuishi.narou.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rakuishi.narou.R
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.Destination

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
        }
    ) {
        NovelList(items = viewModel.novelList.value) { novel ->
            navController.navigate(Destination.createNovelRoute(novel.id))
        }
    }
}

@Composable
fun NovelList(items: List<Novel>, onNovelClicked: (novel: Novel) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items) { _, novel ->
            Column(
                modifier = Modifier
                    .clickable { onNovelClicked.invoke(novel) }
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = novel.title)
            }
        }
    }
}