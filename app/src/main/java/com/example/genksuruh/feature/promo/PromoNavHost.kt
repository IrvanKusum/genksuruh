package com.example.genksuruh.feature.promo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/** Rute internal fitur Promo. Dipanggil dari PromoScreen. */
@Composable
fun PromoNavHost(vm: PromoViewModel, modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "promo_list",
        modifier = modifier
    ) {
        composable("promo_list") {
            if (vm.isProvider) {
                PromoManageScreen(
                    vm = vm,
                    onOpenDetail = { nav.navigate("promo_detail/$it") },
                    onCreate = { nav.navigate("promo_form/new") },
                    onEdit = { nav.navigate("promo_form/$it") }
                )
            } else {
                PromoListScreen(
                    vm = vm,
                    onOpenDetail = { nav.navigate("promo_detail/$it") }
                )
            }
        }

        composable(
            "promo_detail/{promoId}",
            arguments = listOf(navArgument("promoId") { type = NavType.StringType })
        ) { entry ->
            PromoDetailScreen(
                promoId = entry.arguments?.getString("promoId").orEmpty(),
                vm = vm,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("promo_form/$it") },
                onOpenVouchers = {
                    vm.listTab = 1
                    nav.popBackStack("promo_list", inclusive = false)
                }
            )
        }

        composable(
            "promo_form/{promoId}",
            arguments = listOf(navArgument("promoId") { type = NavType.StringType })
        ) { entry ->
            PromoFormScreen(
                promoId = entry.arguments?.getString("promoId").orEmpty(),
                vm = vm,
                onBack = { nav.popBackStack() }
            )
        }
    }
}