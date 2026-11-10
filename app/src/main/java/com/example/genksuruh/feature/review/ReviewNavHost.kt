package com.example.genksuruh.feature.review

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object ReviewRoutes {
    const val LIST = "review_list"
    const val FORM = "review_form/{isEdit}"
    fun form(isEdit: Boolean) = "review_form/$isEdit"
}

@Composable
fun ReviewNavHost(orderId: String, onExit: () -> Unit = {}) {
    val nav = rememberNavController()
    val vm: ReviewViewModel = viewModel(
        key = "review_$orderId",
        factory = ReviewViewModelFactory(orderId),
    )

    NavHost(nav, startDestination = ReviewRoutes.LIST) {
        composable(ReviewRoutes.LIST) {
            ReviewListScreen(
                vm = vm,
                onBack = onExit,
                onWrite = { nav.navigate(ReviewRoutes.form(false)) },
                onEdit = { nav.navigate(ReviewRoutes.form(true)) },
            )
        }
        composable(
            ReviewRoutes.FORM,
            arguments = listOf(navArgument("isEdit") { type = NavType.BoolType }),
        ) { entry ->
            ReviewFormScreen(
                vm = vm,
                isEdit = entry.arguments?.getBoolean("isEdit") ?: false,
                onClose = { nav.popBackStack() },
                onDone = { nav.popBackStack() },
            )
        }
    }
}

@Composable
fun ReviewScreen(orderId: String = "ord_123", onExit: () -> Unit = {}) = ReviewNavHost(orderId, onExit)
