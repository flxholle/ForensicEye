package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class LastLocationDataSource(context: Context) : KeyValueDataSource(context) {
    @SuppressLint("MissingPermission")
    override fun getContentMap(): Map<String, Any> {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val locationProviders = locationManager.allProviders

        return mapOf(
            Pair("lastKnownLocation", lastKnownLocation.toString()),
            Pair("locationProviders", locationProviders)
        )
    }

    override fun getFilename(): String {
        return "last_location.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(
            PermissionWrapper(Manifest.permission.ACCESS_COARSE_LOCATION),
            PermissionWrapper(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }
}