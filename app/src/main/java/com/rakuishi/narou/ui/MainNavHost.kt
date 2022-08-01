package com.rakuishi.narou.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.rakuishi.narou.App
import com.rakuishi.narou.ui.novel_detail.NovelDetailScreen
import com.rakuishi.narou.ui.novel_detail.NovelDetailViewModel
import com.rakuishi.narou.ui.novel_list.NovelListScreen
import com.rakuishi.narou.ui.novel_list.NovelListViewModel

object Destination {
    const val NOVEL_LIST_ROUTE = "novels"
    const val NOVEL_ID = "novel_id"
    const val EPISODE_NUMBER = "episode_number"
    const val NOVEL_DETAIL_ROUTE = "novels/{$NOVEL_ID}/episodes/{$EPISODE_NUMBER}"

    fun createNovelDetailRoute(nid: Long, episodeNumber: Int): String =
        "novels/$nid/episodes/$episodeNumber"
}

@ExperimentalAnimationApi
@Composable
fun MainNavHost(
    app: App,
    navController: NavHostController,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Destination.NOVEL_LIST_ROUTE,
        enterTransition = {
            fadeIn()
        },
        popEnterTransition = {
            fadeIn()
        },
        exitTransition = {
            fadeOut()
        },
        popExitTransition = {
            fadeOut()
        },
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
                navArgument(Destination.EPISODE_NUMBER) { type = NavType.IntType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "narou://novels/{${Destination.NOVEL_ID}}/episodes/{${Destination.EPISODE_NUMBER}}"
                }
            ),
        ) { backStackEntry ->
            val nid = backStackEntry.arguments?.getLong(Destination.NOVEL_ID) ?: 0L
            val episodeNumber = backStackEntry.arguments?.getInt(Destination.EPISODE_NUMBER) ?: 0

            NovelDetailScreen(
                navController,
                viewModel(
                    factory = NovelDetailViewModel.provideFactory(
                        app.novelRepository,
                        app.dataStoreRepository,
                        nid,
                        episodeNumber
                    )
                )
            )
        }
    }
}