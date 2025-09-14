package com.flxholle.forensiceye.data.sources

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class PackageListDataSource(context: Context) : KeyValueDataSource(context, enabled = true) {
    override fun getContentMap(): Map<String, Any> {
        val packageManager: PackageManager = context.packageManager
        val packages: List<PackageInfo> =
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        val packageMap = mutableMapOf<String, Any>()
        for (packageInfo in packages) {
            packageMap[packageInfo.packageName] = mapOf(
                "versionName" to packageInfo.versionName,
                "versionCode" to packageInfo.versionCode,
                "firstInstallTime" to packageInfo.firstInstallTime,
                "lastUpdateTime" to packageInfo.lastUpdateTime
            )
        }

        return packageMap
    }

    override fun getFilename(): String {
        return "packages.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            listOf(
                PermissionWrapper(
                    android.Manifest.permission.QUERY_ALL_PACKAGES,
                    isOptional = true
                )
            )
        } else {
            emptyList()
        }
    }
}