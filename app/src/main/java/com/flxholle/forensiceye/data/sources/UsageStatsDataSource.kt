package com.flxholle.forensiceye.data.sources

import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.flxholle.forensiceye.data.DataSource
import com.flxholle.forensiceye.data.PermissionWrapper
import com.flxholle.forensiceye.utils.UsageStatsEntry
import com.flxholle.forensiceye.utils.UsageStatsWrapper
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class UsageStatsDataSource(context: Context) : DataSource(context, true) {

    /**
     * Writes usage statistics to a CSV file in the external files directory.
     * Uses the UsageStatsWrapper to retrieve usage statistics for the last USAGE_STATS_BACKLOG days.
     * Keep in mind that this API has some errors, but the UsageStatsWrapper should handle them.
     *
     * @return True if the file was written successfully, false otherwise.
     */
    override fun writeToFileInternal(): Boolean {
        try {
            val usagestats = UsageStatsWrapper(context)
            val entries: MutableList<UsageStatsEntry> = mutableListOf()
            val today = LocalDate.now().toEpochDay()
            for (i in (today - USAGE_STATS_BACKLOG)..today) {
                entries.addAll(usagestats.getForegroundStatsByDay(i))
            }

            val externalFile = File(context.getExternalFilesDir(null), "usagestats.csv")
            val fileUri = Uri.fromFile(externalFile)
            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            try {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write("Id,begin,end,beginTimestamp,endTimestamp,packageName,duration,durationMillis\n")

                        for ((id, entry) in entries.withIndex()) {
                            val begin = entry.beginLocalDateTime.format(dateTimeFormatter)
                            val end = entry.endLocalDateTime.format(dateTimeFormatter)
                            val duration = entry.durationLocalDateTime.format(timeFormatter)

                            val row =
                                "${id},$begin,$end,${entry.beginTime},${entry.endTime},${entry.packageName},$duration,${entry.duration}\n"
                            writer.write(row)
                        }
                    }
                }
                Log.d("UsageStats", "File created successfully at $fileUri")
            } catch (e: IOException) {
                Log.e("UsageStats", "Error writing file at $fileUri", e)
                return false
            }
        } catch (e: Exception) {
            Log.e("UsageStats", "Internal error", e)
            return false
        }
        return true
    }

    /**
     * Returns the list of permissions required by this data source. As UsageStats is a special permission, the AppOpsManager String is used.
     * See PermissionWrapper for more details.
     *
     * @return A list of PermissionWrapper objects representing the required permissions.
     */
    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(OPSTR_GET_USAGE_STATS, isSpecial = true))
    }

    companion object {
        /**
         * The number of days to look back for usage statistics.
         */
        const val USAGE_STATS_BACKLOG = 14 // Last X days
    }
}