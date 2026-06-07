package com.ourtimer.app.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Int.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date(this))
}

fun Double.formatPercentage(decimals: Int = 3): String {
    return String.format(Locale.US, "%.${decimals}f%%", this)
}

fun Float.formatPercentage(decimals: Int = 2): String {
    return String.format(Locale.US, "%.${decimals}f%%", this)
}
