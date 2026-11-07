package com.example.genksuruh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.genksuruh.core.auth.AccountRole

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

// User: 5 tab. Favorit dibuka dari menu Profil.
private val userItems = listOf(
    BottomItem(Routes.CATALOG_LIST, "Beranda", Icons.Filled.Home),
    BottomItem(Routes.PROMO_LIST, "Promo", Icons.Filled.LocalOffer),
    BottomItem(Routes.CART_LIST, "Keranjang", Icons.Filled.ShoppingCart),
    BottomItem(Routes.TRACKING_LIST, "Pesanan", Icons.Filled.Receipt),
    BottomItem(Routes.PROFILE, "Profil", Icons.Filled.Person)
)

// Admin: 4 tab. Promo memakai layar Naja, yang menampilkan tambah/hapus untuk admin.
private val adminItems = listOf(
    BottomItem(Routes.ADMIN_SERVICES, "Kelola Jasa", Icons.Filled.Storefront),
    BottomItem(Routes.TRACKING_LIST, "Pesanan Masuk", Icons.Filled.Receipt),
    BottomItem(Routes.PROMO_LIST, "Promo", Icons.Filled.LocalOffer),
    BottomItem(Routes.PROFILE, "Profil", Icons.Filled.Person)
)

fun bottomItemsFor(role: AccountRole): List<BottomItem> =
    if (role == AccountRole.ADMIN) adminItems else userItems

// Layar awal tiap peran
fun startRouteFor(role: AccountRole): String = bottomItemsFor(role).first().route

// Rute yang menampilkan bottom bar. Favorit dibuka dari Profil, bar tetap tampil.
fun bottomBarRoutes(role: AccountRole): Set<String> {
    val routes = bottomItemsFor(role).map { it.route }.toMutableSet()
    if (role == AccountRole.USER) routes += Routes.FAVORITE_LIST
    return routes
}

@Composable
fun AppBottomBar(navController: NavHostController, role: AccountRole) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomItemsFor(role).forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(startRouteFor(role)) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}