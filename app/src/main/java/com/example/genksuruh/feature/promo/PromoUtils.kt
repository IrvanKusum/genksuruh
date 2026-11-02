package com.example.genksuruh.feature.promo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatRupiah(value: Int): String =
    "Rp " + String.format(Locale.US, "%,d", value).replace(',', '.')

private val namaBulan = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

/** "2026-11-30" -> "30 Nov 2026" */
fun formatTanggal(iso: String): String {
    val p = iso.split("-")
    if (p.size != 3) return iso
    return "${p[2].toInt()} ${namaBulan[p[1].toInt() - 1]} ${p[0]}"
}

/** "2026-11-01", "2026-11-30" -> "1 - 30 Nov 2026" */
fun formatPeriode(start: String, end: String): String {
    val s = start.split("-")
    val e = end.split("-")
    return if (s.size == 3 && e.size == 3 && s[0] == e[0] && s[1] == e[1]) {
        "${s[2].toInt()} - ${formatTanggal(end)}"
    } else {
        "${formatTanggal(start)} - ${formatTanggal(end)}"
    }
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