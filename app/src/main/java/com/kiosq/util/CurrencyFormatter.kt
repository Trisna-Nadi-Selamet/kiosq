package com.kiosq.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    fun format(amount: Long): String {
        return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    fun formatShort(amount: Long): String {
        return when {
            amount >= 1_000_000_000 -> "Rp ${String.format("%.1f", amount / 1_000_000_000.0)}M"
            amount >= 1_000_000 -> "Rp ${String.format("%.1f", amount / 1_000_000.0)}jt"
            amount >= 1_000 -> "Rp ${String.format("%.0f", amount / 1_000.0)}rb"
            else -> "Rp $amount"
        }
    }

    fun parse(text: String): Long {
        return text.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0L
    }
}

object DateFormatter {
    private val fullFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val dateOnly = java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val timeOnly = java.text.SimpleDateFormat("HH:mm", Locale("id", "ID"))

    fun formatFull(millis: Long): String = fullFormat.format(java.util.Date(millis))
    fun formatDate(millis: Long): String = dateOnly.format(java.util.Date(millis))
    fun formatTime(millis: Long): String = timeOnly.format(java.util.Date(millis))
}
