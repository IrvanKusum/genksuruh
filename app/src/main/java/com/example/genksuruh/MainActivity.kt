package com.example.genksuruh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.genksuruh.feature.review.ReviewNavHost
import com.example.genksuruh.ui.theme.GenkSuruhTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GenkSuruhTheme {
                ReviewNavHost(onExit = { finish() })
            }
        }
    }
}