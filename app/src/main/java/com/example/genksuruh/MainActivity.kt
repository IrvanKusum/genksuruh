package com.example.genksuruh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.genksuruh.core.auth.AccountRole
import com.example.genksuruh.core.auth.LocalAccountName
import com.example.genksuruh.core.auth.LocalAccountRole
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.feature.auth.LoginScreen
import com.example.genksuruh.feature.auth.SessionViewModel
import com.example.genksuruh.navigation.AppBottomBar
import com.example.genksuruh.navigation.AppNavGraph
import com.example.genksuruh.navigation.bottomBarRoutes

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GenkSuruhTheme {

                val sessionViewModel: SessionViewModel = viewModel()
                val account by sessionViewModel.account.collectAsStateWithLifecycle()
                val current = account

                if (current == null) {
                    LoginScreen()
                } else {
                    // Ganti NavController ketika akun berganti.
                    key(current.id) {
                        MainApp(
                            role = current.role,
                            userName = current.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainApp(
    role: AccountRole,
    userName: String
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    CompositionLocalProvider(
        LocalAccountRole provides role,
        LocalAccountName provides userName
    ) {
        Scaffold(
            bottomBar = {
                if (currentRoute in bottomBarRoutes(role)) {
                    AppBottomBar(navController, role)
                }
            }
        ) { innerPadding ->

            AppNavGraph(
                navController = navController,
                role = role,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}