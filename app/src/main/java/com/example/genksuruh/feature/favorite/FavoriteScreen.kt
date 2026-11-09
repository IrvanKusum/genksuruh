package com.example.genksuruh.feature.favorite

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.genksuruh.feature.favorite.navigation.FavoriteRoutes
import com.example.genksuruh.feature.favorite.navigation.favoriteGraph
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel

@Composable
fun FavoriteScreen(
    onExploreClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val viewModel: FavoriteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = FavoriteRoutes.LIST
    ) {
        favoriteGraph(
            navController = navController,
            viewModel = viewModel,
            onExploreClick = onExploreClick,
            onOrderClick = onOrderClick
        )
    }
}
