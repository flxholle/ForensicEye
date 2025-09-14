package com.flxholle.forensiceye.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * Object representing a timespan that an application was in the foreground.
 *
 * @property beginTime The start time of the usage in milliseconds since epoch.
 * @property endTime The end time of the usage in milliseconds since epoch.
 * @property packageName The package name of the application.
 * @property beginLocalDateTime The start time of the usage as a LocalDateTime.
 * @property endLocalDateTime The end time of the usage as a LocalDateTime.
 * @property duration The duration of the usage in milliseconds.
 * @property durationLocalDateTime The duration of the usage as a LocalDateTime.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class UsageStatsEntry(
    val beginTime: Long,
    val endTime: Long,
    val packageName: String
) {
    // The start time of the usage as a LocalDateTime.
    val beginLocalDateTime: LocalDateTime = Instant.ofEpochMilli(beginTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    // The end time of the usage as a LocalDateTime.
    val endLocalDateTime: LocalDateTime = Instant.ofEpochMilli(endTime)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    // The duration of the usage in milliseconds.
    val duration: Long = endTime - beginTime

    // The duration of the usage as a LocalDateTime.
    val durationLocalDateTime: LocalDateTime = Instant.ofEpochMilli(duration)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}