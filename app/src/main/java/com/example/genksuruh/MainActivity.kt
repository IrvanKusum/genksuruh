package com.example.genksuruh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.genksuruh.navigation.AppNavGraph
import com.example.genksuruh.ui.theme.GenkSuruhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GenkSuruhTheme {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController
                )
            }
        }
    }
}