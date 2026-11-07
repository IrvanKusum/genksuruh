
package com.example.genksuruh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.navigation.AppBottomBar
import com.example.genksuruh.navigation.AppNavGraph
import com.example.genksuruh.navigation.Routes

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GenkSuruhTheme {

                val navController = rememberNavController()

                val backStackEntry by
                navController.currentBackStackEntryAsState()

                val currentRoute = backStackEntry?.destination?.route

                // Bottom Navigation hanya untuk halaman utama.
                val mainRoutes = setOf(
                    Routes.CATALOG_LIST,
                    Routes.CART_LIST,
                    Routes.TRACKING_LIST,
                    Routes.REVIEW_LIST,
                    Routes.FAVORITE_LIST,
                    Routes.PROMO_LIST
                )

                Scaffold(
                    bottomBar = {
                        if (currentRoute in mainRoutes) {
                            AppBottomBar(navController)
                        }
                    }
                ) { innerPadding ->

                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
