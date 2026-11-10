package com.example.genksuruh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.genksuruh.core.util.openWhatsApp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.feature.admin.AdminServicesScreen
import com.example.genksuruh.feature.cart.CartItemDetailScreen
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.cart.CartViewModel
import com.example.genksuruh.feature.cart.CheckoutFailedScreen
import com.example.genksuruh.feature.cart.CheckoutResult
import com.example.genksuruh.feature.cart.CheckoutScreen
import com.example.genksuruh.feature.cart.CheckoutSuccessScreen
import com.example.genksuruh.feature.catalog.CatalogDetailScreen
import com.example.genksuruh.feature.catalog.CatalogFormScreen
import com.example.genksuruh.feature.catalog.CatalogRepository
import com.example.genksuruh.feature.catalog.CatalogScreen
import com.example.genksuruh.feature.favorite.FavoriteScreen
import com.example.genksuruh.feature.profile.EditProfileScreen
import com.example.genksuruh.feature.profile.ProfileScreen
import com.example.genksuruh.feature.promo.PromoScreen
import com.example.genksuruh.feature.review.ReviewScreen
import com.example.genksuruh.feature.tracking.TrackingDetailScreen
import com.example.genksuruh.feature.tracking.TrackingScreen
import com.example.genksuruh.feature.tracking.TrackingUpdateScreen

/**
 * Ubah menjadi true sementara untuk menguji Checkout Failed.
 * Setelah pengujian, kembalikan ke false.
 */
private const val SIMULATE_CHECKOUT_TECHNICAL_FAILURE = false

@Composable
fun AppNavGraph(
    navController: NavHostController,
    role: AccountRole,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel()
) {

    val goToStart: () -> Unit = {
        navController.navigate(startRouteFor(role)) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    // Handler hasil checkout.
    val handleCheckoutResult: (CheckoutResult) -> Unit = { result ->
        when (result) {
            CheckoutResult.SUCCESS -> {
                navController.navigate(Routes.CHECKOUT_SUCCESS) {
                    launchSingleTop = true
                }
            }

            CheckoutResult.FAILED -> {
                navController.navigate(Routes.CHECKOUT_FAILED) {
                    launchSingleTop = true
                }
            }

            CheckoutResult.INSUFFICIENT_BALANCE -> {
                // Tidak berpindah halaman.
                // Peringatan saldo kurang ditampilkan di CheckoutScreen.
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRouteFor(role),
        modifier = modifier
    ) {

        // =====================================================
        // KATALOG (IRVAN)
        // =====================================================

        composable(Routes.CATALOG_LIST) {
            CatalogScreen(
                onServiceClick = { id ->
                    navController.navigate(Routes.catalogDetail(id))
                },
                onAddClick = {},
                showAddButton = false,
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(
            route = Routes.CATALOG_DETAIL,
            arguments = listOf(
                navArgument("serviceId") {
                    type = NavType.StringType
                }
            )
        ) {
            CatalogDetailScreen(
                onBack = { navController.popBackStack() },
                onGoToCart = {
                    navController.navigate(Routes.CART_LIST) { launchSingleTop = true }
                },
                isAdmin = role == AccountRole.ADMIN,
                onEditClick = { id -> navController.navigate(Routes.catalogForm(id)) },
                onBuyNow = { service ->
                    cartViewModel.buyNow(service)
                    navController.navigate(Routes.CHECKOUT) { launchSingleTop = true }
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
            CatalogFormScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        // =====================================================
        // ADMIN (AWA)
        // =====================================================

        composable(Routes.ADMIN_SERVICES) {
            AdminServicesScreen(
                onAddClick = {
                    navController.navigate(Routes.catalogForm())
                },
                onEditClick = { id ->
                    navController.navigate(Routes.catalogForm(id))
                },
                onServiceClick = { id ->
                    navController.navigate(Routes.catalogDetail(id))
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        // =====================================================
        // PROFIL
        // =====================================================

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditProfile = {
                    navController.navigate(Routes.PROFILE_EDIT)
                }
            )
        }

        composable(Routes.PROFILE_EDIT) {
            EditProfileScreen(
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        // =====================================================
        // KERANJANG (RASYID)
        // =====================================================

        composable(Routes.CART_LIST) {
            CartScreen(
                viewModel = cartViewModel,

                onNavigateToCheckout = {
                    navController.navigate(Routes.CHECKOUT)
                },

                onNavigateToItemDetail = { itemId ->
                    cartViewModel.startEditingItem(itemId)

                    navController.navigate(
                        Routes.CART_ITEM_DETAIL
                    )
                },

                onNavigateToCatalog = {
                    goToStart()
                },

                onNavigateToPromo = {
                    navController.navigate(Routes.PROMO_LIST) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // =====================================================
        // DETAIL / EDIT ITEM KERANJANG (RASYID)
        // =====================================================

        composable(Routes.CART_ITEM_DETAIL) {
            CartItemDetailScreen(
                viewModel = cartViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // =====================================================
        // CHECKOUT (RASYID)
        // =====================================================

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                viewModel = cartViewModel,

                onNavigateBack = {
                    navController.popBackStack()
                },

                onPlaceOrder = {
                    cartViewModel.processCheckout(
                        simulateTechnicalFailure =
                            SIMULATE_CHECKOUT_TECHNICAL_FAILURE
                    ) { result ->
                        handleCheckoutResult(result)
                    }
                }
            )
        }

        // =====================================================
        // CHECKOUT FAILED (RASYID)
        // =====================================================

        composable(Routes.CHECKOUT_FAILED) {
            CheckoutFailedScreen(
                viewModel = cartViewModel,

                onRetry = {
                    cartViewModel.resetCheckoutProcess()

                    navController.popBackStack(
                        Routes.CHECKOUT,
                        inclusive = false
                    )

                    // Percobaan ulang menggunakan simulasi berhasil.
                    cartViewModel.processCheckout(
                        simulateTechnicalFailure = false
                    ) { result ->
                        handleCheckoutResult(result)
                    }
                },

                onBackToCheckout = {
                    cartViewModel.resetCheckoutProcess()

                    navController.popBackStack(
                        Routes.CHECKOUT,
                        inclusive = false
                    )
                }
            )
        }

        // =====================================================
        // CHECKOUT SUCCESS (RASYID)
        // =====================================================

        composable(Routes.CHECKOUT_SUCCESS) {
            CheckoutSuccessScreen(
                viewModel = cartViewModel,

                onNavigateToHistory = {
                    navController.navigate(Routes.TRACKING_LIST) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    cartViewModel.finishCheckout()
                },
                onNavigateHome = {
                    navController.navigate(startRouteFor(role)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    cartViewModel.finishCheckout()
                }
            )
        }

        // =====================================================
        // STATUS & RIWAYAT (HILMIY)
        // =====================================================

        composable(Routes.TRACKING_LIST) {
            val context = LocalContext.current
            TrackingScreen(
                onOrderClick = { id ->
                    navController.navigate(Routes.trackingDetail(id))
                },
                onChatClick = { id ->
                    openWhatsApp(context, "085895089845", "Halo Mitra GenkSuruh, saya ingin bertanya mengenai pesanan #$id")
                },
                onExploreServices = goToStart,
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(
            route = Routes.TRACKING_DETAIL,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val context = LocalContext.current
            val statusUpdated by entry.savedStateHandle
                .getStateFlow("statusUpdated", false)
                .collectAsStateWithLifecycle()

            TrackingDetailScreen(
                onBack = {
                    navController.popBackStack()
                },
                onUpdateStatus = { id ->
                    navController.navigate(Routes.trackingUpdate(id))
                },
                onReview = { id ->
                    navController.navigate(Routes.review(id))
                },
                onReorder = {
                    goToStart()
                },
                onChat = { id ->
                    openWhatsApp(context, "085895089845", "Halo Mitra GenkSuruh, saya ingin bertanya mengenai pesanan #$id")
                },
                showStatusUpdatedMessage = statusUpdated,
                onStatusUpdatedMessageShown = {
                    entry.savedStateHandle["statusUpdated"] = false
                }
            )
        }

        composable(
            route = Routes.TRACKING_UPDATE,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) {
            TrackingUpdateScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("statusUpdated", true)

                    navController.popBackStack()
                }
            )
        }

        // =====================================================
        // ULASAN (AHSANA)
        // =====================================================

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val orderId = entry.arguments
                ?.getString("orderId")
                ?: return@composable

            ReviewScreen(
                orderId = orderId,
                onExit = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REVIEW_LIST) {
            ReviewScreen()
        }

        // =====================================================
        // FAVORIT (ALUSHYA)
        // =====================================================

        composable(Routes.FAVORITE_LIST) {
            FavoriteScreen(
                onExploreClick = goToStart,
                onOrderClick = { id ->
                    CatalogRepository.getById(id)?.let { service ->
                        cartViewModel.buyNow(service)
                        navController.navigate(Routes.CHECKOUT) { launchSingleTop = true }
                    }
                }
            )
        }

        // =====================================================
        // PROMO (NAJA)
        // =====================================================

        composable(Routes.PROMO_LIST) {
            PromoScreen(
                onOpenProfile = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }
    }
}