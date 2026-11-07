
package com.example.genksuruh.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.feature.cart.CartItemDetailScreen
import com.example.genksuruh.feature.cart.CartScreen
import com.example.genksuruh.feature.cart.CartViewModel
import com.example.genksuruh.feature.cart.CheckoutFailedScreen
import com.example.genksuruh.feature.cart.CheckoutResult
import com.example.genksuruh.feature.cart.CheckoutScreen
import com.example.genksuruh.feature.cart.CheckoutSuccessScreen
import com.example.genksuruh.feature.catalog.CatalogScreen
import com.example.genksuruh.feature.favorite.FavoriteScreen
import com.example.genksuruh.feature.promo.PromoScreen
import com.example.genksuruh.feature.review.ReviewScreen
import com.example.genksuruh.feature.tracking.TrackingScreen

/**
 * Ubah menjadi true sementara untuk menguji Checkout Failed.
 *
 * Setelah pengujian, kembalikan ke false.
 */
private const val SIMULATE_CHECKOUT_TECHNICAL_FAILURE = false

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    cartViewModel: CartViewModel = viewModel()
) {

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
        startDestination = Routes.CATALOG_LIST,
        modifier = modifier
    ) {

        // =====================================================
        // KATALOG (IRVAN)
        // =====================================================

        composable(Routes.CATALOG_LIST) {
            CatalogScreen()
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
                    navController.navigate(
                        Routes.CATALOG_LIST
                    )
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
                    // Kembali ke Checkout.
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
                    navController.navigate(
                        Routes.TRACKING_LIST
                    ) {
                        popUpTo(Routes.CART_LIST)
                    }
                },

                onNavigateHome = {
                    navController.navigate(
                        Routes.CATALOG_LIST
                    ) {
                        popUpTo(Routes.CATALOG_LIST) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // =====================================================
        // STATUS & RIWAYAT (HILMIY)
        // =====================================================

        composable(Routes.TRACKING_LIST) {
            TrackingScreen()
        }

        // =====================================================
        // ULASAN (AHSANA)
        // =====================================================

        composable(Routes.REVIEW_LIST) {
            ReviewScreen()
        }

        // =====================================================
        // FAVORIT (ALUSHYA)
        // =====================================================

        composable(Routes.FAVORITE_LIST) {
            FavoriteScreen()
        }

        // =====================================================
        // PROMO (NAJA)
        // =====================================================

        composable(Routes.PROMO_LIST) {
            PromoScreen()
        }
    }
}
