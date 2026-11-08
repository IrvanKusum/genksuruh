package com.example.genksuruh.feature.promo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatRupiah(value: Int): String =
    "Rp " + String.format(Locale.US, "%,d", value).replace(',', '.')

private val namaBulan = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

/** "2026-11-30" menjadi "30 Nov 2026" */
fun formatTanggal(iso: String): String {
    val parts = iso.split("-")
    if (parts.size != 3) return iso
    val bulan = parts[1].toIntOrNull() ?: return iso
    val hari = parts[2].toIntOrNull() ?: return iso
    if (bulan !in 1..12) return iso
    return "$hari ${namaBulan[bulan - 1]} ${parts[0]}"
}

fun millisToIso(ms: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(ms))
}

fun copyCode(context: Context, code: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("kode voucher", code))
    Toast.makeText(context, "Kode $code disalin", Toast.LENGTH_SHORT).show()
}

suspend fun SnackbarHostState.showPromoMessage(
    message: String,
    undoable: Boolean = false,
    onUndo: () -> Unit = {}
) {
    currentSnackbarData?.dismiss()
    val result = showSnackbar(
        message = message,
        actionLabel = if (undoable) "Urungkan" else null,
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) onUndo()
}