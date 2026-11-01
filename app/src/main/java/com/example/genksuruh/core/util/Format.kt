package com.example.genksuruh.core.util

import java.text.NumberFormat
import java.util.Locale

fun Int.toRupiah(): String {
    val formatted = NumberFormat.getInstance(Locale("id", "ID")).format(this)
    return "Rp $formatted"
}