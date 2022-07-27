package com.rakuishi.narou.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.rakuishi.narou.App
import com.rakuishi.narou.ui.home.HomeScreen
import com.rakuishi.narou.ui.home.HomeViewModel
import com.rakuishi.narou.ui.novel.NovelScreen
import com.rakuishi.narou.ui.novel.NovelViewModel

object Destination {
    const val HOME_ROUTE = "home"
    const val NOVEL_ID = "novel_id"
    const val NOVEL_ROUTE = "novel/{${NOVEL_ID}}"

    fun createNovelRoute(novelId: Long): String = "novel/$novelId"
}

@ExperimentalAnimationApi
@Composable
fun AppNavHost(
    app: App,
    navController: NavHostController,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Destination.HOME_ROUTE,
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
        composable(Destination.HOME_ROUTE) {
            HomeScreen(
                navController,
                viewModel(
                    factory = HomeViewModel.provideFactory(app.novelRepository)
                )
            )
        }
        composable(
            Destination.NOVEL_ROUTE,
            arguments = listOf(navArgument(Destination.NOVEL_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val novelId = backStackEntry.arguments?.getLong(Destination.NOVEL_ID) ?: 0
            NovelScreen(
                navController,
                viewModel(
                    factory = NovelViewModel.provideFactory(
                        app.novelRepository,
                        app.dataStoreRepository,
                        novelId
                    )
                )
            )
        }
    }
}