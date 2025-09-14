package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

/**
 * Data source for retrieving Wi-Fi network information.
 * Only available to device owner apps, therefore disabled.
 *
 * @param context The application context.
 */
class WifiNetworksDataSource(context: Context) : KeyValueDataSource(context, enabled = false) {

    /**
     * Retrieves a map of Wi-Fi network information.
     *
     * @return A map where the key is the SSID and the value is a map of network properties.
     */
    @SuppressLint("MissingPermission")
    override fun getContentMap(): Map<String, Any> {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val configuredNetworks = wifiManager.configuredNetworks

        val wifiMap = mutableMapOf<String, Any>()

        for (network in configuredNetworks) {
            val networkMap: Map<String, Any> = mapOf(
                "BSSID" to network.BSSID,
                "hiddenSSID" to network.hiddenSSID,
                "status" to network.status,
                "priority" to network.priority,
                "allowedKeyManagement" to network.allowedKeyManagement,
                "allowedProtocols" to network.allowedProtocols,
                "allowedAuthAlgorithms" to network.allowedAuthAlgorithms,
                "allowedPairwiseCiphers" to network.allowedPairwiseCiphers,
                "allowedGroupCiphers" to network.allowedGroupCiphers,
                "preSharedKey" to network.preSharedKey,
                "wepKeys" to network.wepKeys,
                "wepTxKeyIndex" to network.wepTxKeyIndex,
                "enterpriseConfig" to network.enterpriseConfig,
            )
            wifiMap[network.SSID] = networkMap
        }

        return wifiMap
    }


    override fun getFilename(): String {
        return "wifi_networks.json"
    }


    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(
            PermissionWrapper(Manifest.permission.ACCESS_WIFI_STATE),
            PermissionWrapper(Manifest.permission.ACCESS_COARSE_LOCATION),
            PermissionWrapper(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }
}