package com.example.genksuruh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.catalog.CatalogDetailScreen
import com.example.genksuruh.feature.catalog.CatalogFormScreen
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
    // Pindah ke tab Beranda (dipakai tombol "Eksplor Layanan" dan "Pesan Lagi")
    val goToCatalog: () -> Unit = {
        navController.navigate(Routes.CATALOG_LIST) {
            popUpTo(Routes.CATALOG_LIST) { inclusive = false }
            launchSingleTop = true
        }
    }

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
                onGoToCart = {
                    navController.navigate(Routes.CART_LIST) { launchSingleTop = true }
                }
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
        composable(Routes.TRACKING_LIST) {
            TrackingScreen(
                onOrderClick = { orderId -> navController.navigate(Routes.trackingDetail(orderId)) },
                onExploreServices = goToCatalog
            )
        }
        composable(
            route = Routes.TRACKING_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            // Pesan "status berhasil diperbarui" dikirim dari layar update lewat savedStateHandle
            val statusUpdated by entry.savedStateHandle
                .getStateFlow("statusUpdated", false)
                .collectAsStateWithLifecycle()

            TrackingDetailScreen(
                onBack = { navController.popBackStack() },
                onUpdateStatus = { orderId -> navController.navigate(Routes.trackingUpdate(orderId)) },
                onReview = { orderId -> navController.navigate(Routes.review(orderId)) },
                onReorder = { goToCatalog() },
                showStatusUpdatedMessage = statusUpdated,
                onStatusUpdatedMessageShown = { entry.savedStateHandle["statusUpdated"] = false }
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
                        ?.savedStateHandle
                        ?.set("statusUpdated", true)
                    navController.popBackStack()
                }
            )
        }

        // ===== Ulasan (Ahsana) =====
        composable(
            route = Routes.REVIEW,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            ReviewScreen(
                orderId = orderId,
                onExit = { navController.popBackStack() }
            )
        }

        // ===== Favorit (Alushya) =====
        composable(Routes.FAVORITE_LIST) { FavoriteScreen() }

        // ===== Promo (Naja) =====
        composable(Routes.PROMO_LIST) { PromoScreen() }
    }
}