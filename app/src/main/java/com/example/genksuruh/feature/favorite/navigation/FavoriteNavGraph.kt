package com.example.genksuruh.feature.favorite.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.genksuruh.feature.favorite.ui.FavoriteDetailScreen
import com.example.genksuruh.feature.favorite.ui.FavoriteListScreen
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel

object FavoriteRoutes {
    const val LIST = "favorit_list"
    const val DETAIL = "favorit_detail/{id}"
    fun detail(id: String) = "favorit_detail/$id"
}

fun NavGraphBuilder.favoriteGraph(
    navController: NavHostController,
    viewModel: FavoriteViewModel,
    onExploreClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    composable(FavoriteRoutes.LIST) {
        FavoriteListScreen(
            viewModel = viewModel,
            onItemClick = { id -> navController.navigate(FavoriteRoutes.detail(id)) },
            onExploreClick = onExploreClick
        )
    }

    composable(
        route = FavoriteRoutes.DETAIL,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { entry ->
        val id = entry.arguments?.getString("id").orEmpty()
        FavoriteDetailScreen(
            serviceId = id,
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onOrderClick = onOrderClick
        )
    }
}
