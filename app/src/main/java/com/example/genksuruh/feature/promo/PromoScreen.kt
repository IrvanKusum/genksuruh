package com.example.genksuruh.feature.promo

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Pintu masuk fitur Promo & Voucher.
 * Panggil `PromoScreen()` dari MainActivity (untuk tes) atau dari NavHost utama tim:
 *     composable("promo") { PromoScreen() }
 */
@Composable
fun PromoScreen(vm: PromoViewModel = viewModel()) {
    val snackbar = remember { SnackbarHostState() }
    var currentEvent by remember { mutableStateOf<UiEvent?>(null) }

    LaunchedEffect(Unit) {
        vm.events.collect { e ->
            currentEvent = e
            val result = snackbar.showSnackbar(
                message = e.message,
                actionLabel = e.actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) e.onAction?.invoke()
        }
    }

    PromoTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbar, modifier = Modifier.padding(bottom = 16.dp)) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (currentEvent?.success == true) PromoColors.Aktif
                        else Color(0xFF263238),
                        contentColor = Color.White,
                        actionColor = PromoColors.Sand
                    )
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { inner ->
            PromoNavHost(
                vm = vm,
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize()
            )
        }
    }
}