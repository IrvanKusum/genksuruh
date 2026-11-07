package com.example.genksuruh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.catalog.CatalogScreen
import com.example.genksuruh.feature.favorite.FavoriteScreen
import com.example.genksuruh.feature.promo.PromoScreen
import com.example.genksuruh.feature.review.ReviewScreen
import com.example.genksuruh.feature.tracking.TrackingDetailScreen
import com.example.genksuruh.feature.tracking.TrackingScreen
import com.example.genksuruh.feature.tracking.TrackingUpdateScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.TRACKING_LIST,
        modifier = modifier
    ) {
        composable(Routes.CATALOG_LIST) { CatalogScreen() }
        composable(Routes.CART_LIST) { CartScreen() }

        composable(Routes.TRACKING_LIST) {
            TrackingScreen(
                onOrderClick = { id -> navController.navigate(Routes.trackingDetail(id)) }
            )
        }

        composable(
            route = Routes.TRACKING_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val updated by entry.savedStateHandle
                .getStateFlow("status_updated", false)
                .collectAsState()

            TrackingDetailScreen(
                onBack = { navController.popBackStack() },
                onUpdateStatus = { id -> navController.navigate(Routes.trackingUpdate(id)) },
                // PENTING: orderId dari pesanan yang sedang dibuka diteruskan ke Review.
                onReview = { orderId -> navController.navigate(Routes.review(orderId)) },
                showStatusUpdatedMessage = updated,
                onStatusUpdatedMessageShown = {
                    entry.savedStateHandle["status_updated"] = false
                }
            )
        }

        composable(
            route = Routes.TRACKING_UPDATE,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            TrackingUpdateScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("status_updated", true)
                    navController.popBackStack()
                }
            )
        }

        // Review menerima orderId agar jasa yang ditampilkan sesuai pesanan pilihan.
        composable(
            route = Routes.REVIEW_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val orderId = checkNotNull(entry.arguments?.getString("orderId"))
            ReviewScreen(
                orderId = orderId,
                onExit = { navController.popBackStack() }
            )
        }

        composable(Routes.FAVORITE_LIST) { FavoriteScreen() }
        composable(Routes.PROMO_LIST) { PromoScreen() }
    }
}
