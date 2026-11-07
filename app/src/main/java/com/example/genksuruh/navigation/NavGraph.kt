package com.example.genksuruh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.catalog.CatalogScreen
import com.example.genksuruh.feature.favorite.FavoriteScreen
import com.example.genksuruh.feature.promo.PromoScreen
import com.example.genksuruh.feature.review.ReviewScreen
import com.example.genksuruh.feature.tracking.TrackingScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.genksuruh.feature.catalog.CatalogDetailScreen
import com.example.genksuruh.feature.catalog.CatalogFormScreen
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CATALOG_LIST,
        modifier = modifier
    ) {
        // ===== Katalog (Irvan) =====
        composable(Routes.CATALOG_LIST) {
            CatalogScreen(
                onServiceClick = { id -> navController.navigate(Routes.catalogDetail(id)) },
                onAddClick = { navController.navigate(Routes.catalogForm()) }
            )
        }
        composable(
            route = Routes.CATALOG_DETAIL,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) {
            CatalogDetailScreen(
                onBack = { navController.popBackStack() },
                onGoToCart = { navController.navigate(Routes.CART_LIST) }
            )
        }
        composable(
            route = Routes.CATALOG_FORM,
            arguments = listOf(
                navArgument("serviceId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            CatalogFormScreen(onClose = { navController.popBackStack() })
        }

        // ===== Keranjang (Rasyid) =====
        composable(Routes.CART_LIST) { CartScreen() }

        // ===== Status & Riwayat (Hilmiy) =====
        composable(Routes.TRACKING_LIST) { TrackingScreen() }

        // ===== Ulasan (Ahsana) =====
        composable(Routes.REVIEW_LIST) { ReviewScreen() }

        // ===== Favorit (Alushya) =====
        composable(Routes.FAVORITE_LIST) { FavoriteScreen() }

        // ===== Promo (Naja) =====
        composable(Routes.PROMO_LIST) { PromoScreen() }
    }
}