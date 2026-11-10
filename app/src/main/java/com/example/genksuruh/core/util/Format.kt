package com.example.genksuruh.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale

fun Int.toRupiah(): String {
    val formatted = NumberFormat.getInstance(Locale("id", "ID")).format(this)
    return "Rp $formatted"
}

fun openWhatsApp(
    context: Context,
    phoneNumber: String = "085895089845",
    message: String = "Halo Mitra GenkSuruh, saya ingin bertanya tentang pesanan/jasa Anda."
) {
    val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
    val formattedNumber = when {
        cleanNumber.startsWith("+62") -> cleanNumber.substring(1)
        cleanNumber.startsWith("62") -> cleanNumber
        cleanNumber.startsWith("0") -> "62" + cleanNumber.substring(1)
        else -> "62$cleanNumber"
    }
    val url = "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
    }
}