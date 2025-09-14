package com.flxholle.forensiceye.data.sources

import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.storage.StorageManager
import com.flxholle.forensiceye.data.PermissionWrapper
import com.flxholle.forensiceye.data.KeyValueDataSource

class StorageStatsDataSource(context: Context) : KeyValueDataSource(context) {
    override fun getContentMap(): Map<String, Any> {
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val totalBytes = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
        val freeBytes = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)

        return mapOf(
            Pair("totalBytes", totalBytes),
            Pair("freeBytes", freeBytes)
        )
    }

    override fun getFilename(): String {
        return "storage_stats.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(OPSTR_GET_USAGE_STATS, isSpecial = true))
    }
}