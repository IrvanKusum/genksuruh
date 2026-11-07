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
        composable(Routes.CATALOG_LIST) { CatalogScreen() }

        // ===== Keranjang (Rasyid) =====
        composable(Routes.CART_LIST) { CartScreen() }

        // ===== Status & Riwayat (Hilmiy) =====
        composable(Routes.TRACKING_LIST) { TrackingScreen() }

        // ===== Ulasan (Ahsana) =====
        composable(Routes.REVIEW_LIST) {
            ReviewScreen(onExit = { navController.popBackStack() })
        }

        // ===== Favorit (Alushya) =====
        composable(Routes.FAVORITE_LIST) { FavoriteScreen() }

        // ===== Promo (Naja) =====
        composable(Routes.PROMO_LIST) { PromoScreen() }
    }
}