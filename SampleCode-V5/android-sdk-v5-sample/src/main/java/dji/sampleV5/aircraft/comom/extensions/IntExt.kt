package dji.sampleV5.aircraft.comom.extensions

import android.content.Context
import kotlin.math.roundToInt

fun Int.toPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

fun Float.toPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).roundToInt()

fun Int.toDp(context: Context): Int =
        (this / context.resources.displayMetrics.density).toInt()