package com.flxholle.forensiceye.data.sources

import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStats
import android.app.usage.NetworkStats.Bucket
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import android.os.Build
import android.os.RemoteException
import android.util.Log
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

/**
 * Data source for retrieving network statistics.
 *
 * @param context The application context.
 */
class NetworkStatsDataSource(context: Context) : KeyValueDataSource(context) {

    /**
     * Retrieves a map of network statistics. More could be collected, if the app would be the default SMS app.
     * See BlockedNumbersDataSource for more information.
     *
     * @return A map containing mobile and Wi-Fi network statistics.
     */
    override fun getContentMap(): Map<String, Any> {
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val bucket = NetworkStats.Bucket()
        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - NETWORK_BACKLOG

        val returnMap = mutableMapOf<String, Any>()

        try {
            // Needs to be default SMS app, if not use null
            val subscriberId =
//                (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).subscriberId
                null

            // Query mobile data usage
            val mobileMap = mutableMapOf<String, Any>()
            val mobileStats: NetworkStats = networkStatsManager.queryDetails(
                NetworkCapabilities.TRANSPORT_CELLULAR, subscriberId, startTime, currentTime
            )
            while (mobileStats.hasNextBucket()) {
                if (mobileStats.getNextBucket(bucket))
                    mobileMap["${bucket.uid}"] = getNetworkStats(bucket)
            }

            // Query Wi-Fi data usage
            val wifiMap = mutableMapOf<String, Any>()
            val wifiStats = networkStatsManager.queryDetails(
                NetworkCapabilities.TRANSPORT_WIFI, subscriberId, startTime, currentTime
            )
            while (wifiStats.hasNextBucket()) {
                if (wifiStats.getNextBucket(bucket))
                    wifiMap["${bucket.uid}"] = getNetworkStats(bucket)
            }

            returnMap["mobileStats"] = mobileMap
            returnMap["wifiStats"] = wifiMap
        } catch (e: RemoteException) {
            Log.e("NetworkStats", "Error querying network stats", e)
        }
        return returnMap
    }

    /**
     * Converts a NetworkStats.Bucket object to a map of network statistics.
     *
     * @param bucket The NetworkStats.Bucket object.
     * @return A map containing the network statistics.
     */
    private fun getNetworkStats(bucket: Bucket): Map<String, Any> {
        return mapOf(
            "rxBytes" to bucket.rxBytes,
            "txBytes" to bucket.txBytes,
            "rxPackets" to bucket.rxPackets,
            "txPackets" to bucket.txPackets,
            "startTimeStamp" to bucket.startTimeStamp,
            "endTimeStamp" to bucket.endTimeStamp,
            "tag" to bucket.tag,
            "state" to bucket.state,
            "defaultNetworkStatus" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) bucket.defaultNetworkStatus else "unavailable",
            "metered" to bucket.metered,
            "roaming" to bucket.roaming
        )
    }


    override fun getFilename(): String {
        return "network_stats.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(OPSTR_GET_USAGE_STATS, isSpecial = true))
    }

    companion object {
        const val NETWORK_BACKLOG = 24 * 60 * 60 * 1000L // Last 24 hours
    }
}