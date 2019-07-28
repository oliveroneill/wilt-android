package com.oliveroneill.wilt.viewmodel

import androidx.annotation.VisibleForTesting

/**
 * Convert milliseconds into a duration as a human readable string
 */
@VisibleForTesting
fun Long.toDurationFromMilliseconds(): String {
    if (this < 0) return ""
    if (this == 0L) return "0 seconds"
    // Don't worry about milliseconds so we'll just remove them
    val x = this / 1000
    var divisor = 7 * 24 * 60 * 60
    // Calculate weeks
    val weeks = x / divisor
    var remainder= x % divisor
    divisor /= 7
    // Calculate days
    val days= remainder / divisor
    remainder %= divisor
    divisor /= 24
    // Calculate hours
    val hours = remainder / divisor
    remainder %= divisor
    divisor /= 60
    // Calculate minutes and seconds
    val minutes = remainder / divisor
    val seconds = remainder % divisor
    // Create string
    var result =
        if (weeks > 1) "$weeks weeks "
        else if (weeks > 0) "$weeks week "
        else "" +
                if (days > 1) "$days days "
                else if (days > 0) "$days day "
                else "" +
                        if (hours > 1) "$hours hours "
                        else if (hours > 0) "$hours hour "
                        else "" +
                                if (minutes > 1) "$minutes minutes "
                                else if (minutes > 0) "$minutes minute "
                                else ""
    if (seconds > 1)
        result += "$seconds seconds"
    else if (seconds > 0)
        result += "$seconds second"
    else {
        // Delete the last space if there are no seconds
        result = result.substring(0, result.length - 1)
    }
    return result
}
