package com.example.genksuruh.feature.favorite.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.genksuruh.feature.favorite.ui.CatalogExploreScreen
import com.example.genksuruh.feature.favorite.ui.FavColors
import com.example.genksuruh.feature.favorite.ui.FavType
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel

private const val ROUTE_BERANDA = "beranda"
private const val ROUTE_KERANJANG = "keranjang"
private const val ROUTE_PROMO = "promo"
private const val ROUTE_PROFIL = "profil"
private const val ROUTE_CHECKOUT = "checkout"

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

private val bottomItems = listOf(
    BottomItem(ROUTE_BERANDA, "Beranda", Icons.Rounded.Home),
    BottomItem(ROUTE_KERANJANG, "Keranjang", Icons.Rounded.ShoppingBag),
    BottomItem(FavoriteRoutes.LIST, "Favorit", Icons.Rounded.FavoriteBorder, Icons.Rounded.Favorite),
    BottomItem(ROUTE_PROMO, "Promo", Icons.Rounded.LocalOffer),
    BottomItem(ROUTE_PROFIL, "Profil", Icons.Rounded.Person)
)

@Composable
fun DemoAppShell() {
    val navController = rememberNavController()
    val favoriteViewModel: FavoriteViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && bottomItems.any { it.route == currentRoute }

    Scaffold(
        containerColor = FavColors.Bg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            if (showBottomBar) {
                GenkBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { route -> navigateTopLevel(navController, route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FavoriteRoutes.LIST,
            modifier = Modifier.padding(innerPadding)
        ) {
            favoriteGraph(
                navController = navController,
                viewModel = favoriteViewModel,
                onExploreClick = { navigateTopLevel(navController, ROUTE_BERANDA) },
                onOrderClick = { navController.navigate(ROUTE_CHECKOUT) }
            )

            composable(ROUTE_BERANDA) {
                CatalogExploreScreen(
                    viewModel = favoriteViewModel,
                    onItemClick = { id -> navController.navigate(FavoriteRoutes.detail(id)) },
                    onNavigateToFavorites = { navigateTopLevel(navController, FavoriteRoutes.LIST) }
                )
            }
            composable(ROUTE_KERANJANG) {
                PlaceholderScreen("Keranjang", "Halaman ini belum dikerjakan.")
            }
            composable(ROUTE_PROMO) {
                PlaceholderScreen("Promo", "Modul Promo & Voucher dikerjakan di branch lain.")
            }
            composable(ROUTE_PROFIL) {
                PlaceholderScreen("Profil", "Halaman profil dikerjakan di branch lain.")
            }
            composable(ROUTE_CHECKOUT) {
                PlaceholderScreen(
                    title = "Checkout",
                    subtitle = "Modul Pemesanan dikerjakan di branch lain.",
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun GenkBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Surface(color = FavColors.Bg, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            bottomItems.forEach { item ->
                val selected = item.route == currentRoute
                val color = if (selected) FavColors.Primary500 else FavColors.TextSecondary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onItemClick(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                            .background(if (selected) FavColors.Primary500 else Color.Transparent)
                    )
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(text = item.label, style = FavType.label11.copy(color = color))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Kembali",
                    tint = FavColors.TextMain,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = FavType.title18, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(text = subtitle, style = FavType.body14, textAlign = TextAlign.Center)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FavColors.Primary500)
                ) {
                    Text(text = actionLabel, style = FavType.button14)
                }
            }
        }
    }
}
