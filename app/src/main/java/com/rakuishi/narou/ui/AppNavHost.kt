package com.rakuishi.narou.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rakuishi.narou.App
import com.rakuishi.narou.ui.home.HomeScreen
import com.rakuishi.narou.ui.home.HomeViewModel
import com.rakuishi.narou.ui.novel.NovelScreen
import com.rakuishi.narou.ui.novel.NovelViewModel

object Destination {
    const val HOME_ROUTE = "home"
    const val NOVEL_ID = "novel_id"
    const val NOVEL_ROUTE = "novel/{${NOVEL_ID}}"

    fun createNovelRoute(novelId: Int): String = "novel/$novelId"
}

@Composable
fun AppNavHost(
    app: App,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Destination.HOME_ROUTE
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
            arguments = listOf(navArgument(Destination.NOVEL_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val novelId = backStackEntry.arguments?.getInt(Destination.NOVEL_ID) ?: 0
            NovelScreen(
                navController,
                viewModel(
                    factory = NovelViewModel.provideFactory(app.novelRepository, novelId)
                )
            )
        }
    }
}