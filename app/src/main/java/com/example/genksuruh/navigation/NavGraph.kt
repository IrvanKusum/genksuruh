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
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.feature.admin.AdminServicesScreen
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.catalog.CatalogDetailScreen
import com.example.genksuruh.feature.catalog.CatalogFormScreen
import com.example.genksuruh.feature.catalog.CatalogScreen
import com.example.genksuruh.feature.favorite.FavoriteScreen
import com.example.genksuruh.feature.profile.EditProfileScreen
import com.example.genksuruh.feature.profile.ProfileScreen
import com.example.genksuruh.feature.promo.PromoScreen
import com.example.genksuruh.feature.review.ReviewScreen
import com.example.genksuruh.feature.tracking.TrackingDetailScreen
import com.example.genksuruh.feature.tracking.TrackingScreen
import com.example.genksuruh.feature.tracking.TrackingUpdateScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    role: AccountRole,
    modifier: Modifier = Modifier
) {
    val goToStart: () -> Unit = {
        navController.navigate(startRouteFor(role)) {
            popUpTo(startRouteFor(role)) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRouteFor(role),
        modifier = modifier
    ) {
        // ===== Katalog (Irvan): sisi pengguna =====
        composable(Routes.CATALOG_LIST) {
            CatalogScreen(
                onServiceClick = { id -> navController.navigate(Routes.catalogDetail(id)) },
                onAddClick = {},
                showAddButton = false // pengguna tidak boleh menambah jasa
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

        // ===== Admin: kelola jasa =====
        composable(Routes.ADMIN_SERVICES) {
            AdminServicesScreen(
                onAddClick = { navController.navigate(Routes.catalogForm()) },
                onEditClick = { id -> navController.navigate(Routes.catalogForm(id)) },
                onServiceClick = { id -> navController.navigate(Routes.catalogDetail(id)) }
            )
        }

        // ===== Profil =====
        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                onOpenPromo = { navController.navigate(Routes.PROMO_LIST) }
            )
        }
        composable(Routes.PROFILE_EDIT) {
            EditProfileScreen(onClose = { navController.popBackStack() })
        }

        // ===== Keranjang (Rasyid) =====
        composable(Routes.CART_LIST) { CartScreen() }

        // ===== Status & Riwayat (Hilmiy) =====
        composable(Routes.TRACKING_LIST) {
            TrackingScreen(
                onOrderClick = { id -> navController.navigate(Routes.trackingDetail(id)) },
                onExploreServices = goToStart
            )
        }
        composable(
            route = Routes.TRACKING_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            val statusUpdated by entry.savedStateHandle
                .getStateFlow("statusUpdated", false)
                .collectAsStateWithLifecycle()

            TrackingDetailScreen(
                onBack = { navController.popBackStack() },
                onUpdateStatus = { id -> navController.navigate(Routes.trackingUpdate(id)) },
                onReview = { id -> navController.navigate(Routes.review(id)) },
                onReorder = { goToStart() },
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
            ReviewScreen(orderId = orderId, onExit = { navController.popBackStack() })
        }

        // ===== Favorit & Promo =====
        composable(Routes.FAVORITE_LIST) { FavoriteScreen() }
        composable(Routes.PROMO_LIST) { PromoScreen() }
    }
}