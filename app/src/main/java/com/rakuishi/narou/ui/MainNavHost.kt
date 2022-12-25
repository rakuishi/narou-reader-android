package com.rakuishi.narou.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rakuishi.narou.App
import com.rakuishi.narou.model.Novel
import com.rakuishi.narou.ui.novel_detail.NovelDetailScreen
import com.rakuishi.narou.ui.novel_detail.NovelDetailViewModel
import com.rakuishi.narou.ui.novel_list.NovelListScreen
import com.rakuishi.narou.ui.novel_list.NovelListViewModel

object Destination {
    const val NOVEL_LIST_ROUTE = "novels"
    const val NOVEL_ID = "novel_id"
    const val EPISODE_ID = "episode_id"
    const val NOVEL_DETAIL_ROUTE = "novels/{$NOVEL_ID}/episodes/{$EPISODE_ID}"

    fun createNovelDetailRoute(novel: Novel): String =
        if (novel.hasNewEpisode && novel.currentEpisodeNumber == novel.latestEpisodeNumber - 1) "novels/${novel.id}/episodes/${novel.latestEpisodeId}"
        else "novels/${novel.id}/episodes/${novel.currentEpisodeId}"
}

@ExperimentalAnimationApi
@Composable
fun MainNavHost(
    app: App,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.NOVEL_LIST_ROUTE,
    ) {
        composable(Destination.NOVEL_LIST_ROUTE) {
            NovelListScreen(
                navController,
                viewModel(
                    factory = NovelListViewModel.provideFactory(app.novelRepository)
                )
            )
        }
        composable(
            Destination.NOVEL_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(Destination.NOVEL_ID) { type = NavType.LongType },
                navArgument(Destination.EPISODE_ID) { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "narou://novels/{${Destination.NOVEL_ID}}/episodes/{${Destination.EPISODE_ID}}"
                }
            ),
        ) { backStackEntry ->
            val novelId = backStackEntry.arguments?.getLong(Destination.NOVEL_ID) ?: 0L
            val episodeId = backStackEntry.arguments?.getString(Destination.EPISODE_ID) ?: ""

            NovelDetailScreen(
                navController,
                viewModel(
                    factory = NovelDetailViewModel.provideFactory(
                        app.novelRepository,
                        app.dataStoreRepository,
                        novelId,
                        episodeId
                    )
                )
            )
        }
    }
}