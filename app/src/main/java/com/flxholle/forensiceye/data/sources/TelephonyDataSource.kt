package com.flxholle.forensiceye.data.sources

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import com.flxholle.forensiceye.data.PermissionWrapper
import com.flxholle.forensiceye.data.KeyValueDataSource

// does not meet the requirements to access device identifiers.
class TelephonyDataSource(context: Context) : KeyValueDataSource(context, enabled = false) {
    override fun getContentMap(): Map<String, Any> {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Get device ID (IMEI)
        val deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            telephonyManager.imei
        } else {
            telephonyManager.deviceId
        }

        // Get network operator name
        val networkOperatorName = telephonyManager.networkOperatorName

        // Get SIM operator name
        val simOperatorName = telephonyManager.simOperatorName

        // Get SIM state
        val simState = telephonyManager.simState

        // Get subscriber ID (IMSI)
        val subscriberId = telephonyManager.subscriberId

        return mapOf(
            Pair("deviceId", deviceId),
            Pair("networkOperatorName", networkOperatorName),
            Pair("simOperatorName", simOperatorName),
            Pair("simState", simState),
            Pair("subscriberId", subscriberId)
        )
    }

    override fun getFilename(): String {
        return "telephony.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf()
    }
}