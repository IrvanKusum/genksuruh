package com.example.genksuruh.feature.promo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.filter

object PromoRoutes {
    const val LIST = "promo_list"
    const val DETAIL = "promo_detail/{promoId}"
    const val FORM = "promo_form/{promoId}"

    fun detail(id: String) = "promo_detail/$id"
    fun form(id: String) = "promo_form/$id"
}

object PromoNavKeys {
    const val FLASH = "flash_message"
    const val FLASH_UNDO = "flash_undo"
    const val OPEN_TAB = "open_tab"
}

/**
 * Pintu masuk fitur Promo & Voucher. Dipanggil dari PromoScreen.
 * [onOpenProfile] dipanggil saat tombol profil di header diketuk.
 */
@Composable
fun PromoNavHost(onOpenProfile: () -> Unit = {}) {
    val nav = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    PromoTheme {
        NavHost(navController = nav, startDestination = PromoRoutes.LIST) {

            composable(PromoRoutes.LIST) { entry ->
                val viewModel: PromoListViewModel = viewModel(factory = PromoModule.listFactory)
                ObserveFlash(entry.savedStateHandle, snackbarHostState) {
                    viewModel.onUndoDelete()
                }
                ObserveOpenTab(entry.savedStateHandle) { tab ->
                    viewModel.onTabSelected(tab)
                }
                PromoListRoute(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onOpenDetail = { id -> nav.navigate(PromoRoutes.detail(id)) },
                    onCreate = { nav.navigate(PromoRoutes.form(PromoFormViewModel.NEW_ID)) },
                    onEdit = { id -> nav.navigate(PromoRoutes.form(id)) },
                    onOpenProfile = onOpenProfile
                )
            }

            composable(
                route = PromoRoutes.DETAIL,
                arguments = listOf(navArgument("promoId") { type = NavType.StringType })
            ) { entry ->
                val promoId = entry.arguments?.getString("promoId").orEmpty()
                val viewModel: PromoDetailViewModel = viewModel(
                    key = "detail_$promoId",
                    factory = PromoModule.detailFactory(promoId)
                )
                PromoDetailRoute(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { nav.popBackStack() },
                    onEdit = { id -> nav.navigate(PromoRoutes.form(id)) },
                    onOpenVouchers = {
                        nav.getBackStackEntry(PromoRoutes.LIST)
                            .savedStateHandle[PromoNavKeys.OPEN_TAB] = 1
                        nav.popBackStack(PromoRoutes.LIST, inclusive = false)
                    },
                    onDeleted = {
                        nav.getBackStackEntry(PromoRoutes.LIST).savedStateHandle
                            .flash("Promo berhasil dihapus", undoable = true)
                        nav.popBackStack(PromoRoutes.LIST, inclusive = false)
                    }
                )
            }

            composable(
                route = PromoRoutes.FORM,
                arguments = listOf(navArgument("promoId") { type = NavType.StringType })
            ) { entry ->
                val promoId = entry.arguments?.getString("promoId").orEmpty()
                val viewModel: PromoFormViewModel = viewModel(
                    key = "form_$promoId",
                    factory = PromoModule.formFactory(promoId)
                )
                PromoFormRoute(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { nav.popBackStack() },
                    onSaved = { message ->
                        nav.previousBackStackEntry?.savedStateHandle?.flash(message)
                        nav.popBackStack()
                    }
                )
            }
        }
    }
}

private fun SavedStateHandle.flash(message: String, undoable: Boolean = false) {
    this[PromoNavKeys.FLASH_UNDO] = undoable
    this[PromoNavKeys.FLASH] = message
}

/** Menampilkan pesan yang dititipkan layar sebelumnya (mis. "Promo berhasil dibuat"). */
@Composable
private fun ObserveFlash(
    handle: SavedStateHandle,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit
) {
    val currentOnUndo by rememberUpdatedState(onUndo)
    LaunchedEffect(handle) {
        handle.getStateFlow(PromoNavKeys.FLASH, "")
            .filter { it.isNotEmpty() }
            .collect { message ->
                val undoable = handle.get<Boolean>(PromoNavKeys.FLASH_UNDO) ?: false
                handle[PromoNavKeys.FLASH] = ""
                handle[PromoNavKeys.FLASH_UNDO] = false
                snackbarHostState.showPromoMessage(message, undoable) { currentOnUndo() }
            }
    }
}

/** Dipakai saat "Lihat Voucher Saya": daftar otomatis pindah ke tab Voucher Saya. */
@Composable
private fun ObserveOpenTab(handle: SavedStateHandle, onOpenTab: (Int) -> Unit) {
    val currentOnOpenTab by rememberUpdatedState(onOpenTab)
    LaunchedEffect(handle) {
        handle.getStateFlow(PromoNavKeys.OPEN_TAB, -1)
            .filter { it >= 0 }
            .collect { tab ->
                handle[PromoNavKeys.OPEN_TAB] = -1
                currentOnOpenTab(tab)
            }
    }
}