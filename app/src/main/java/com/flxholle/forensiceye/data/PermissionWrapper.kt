package com.flxholle.forensiceye.data

import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat


/**
 * Data class representing a permission wrapper.
 *
 * @property name The name of the permission.
 * @property isRuntime Indicates if the permission is a runtime permission.
 * @property isADB Indicates if the permission is an ADB (Android Debug Bridge) permission.
 * @property isOptional Indicates if the permission is optional.
 * @property isSpecial Indicates if the permission is a special permission.
 */
data class PermissionWrapper(
    val name: String,
    val isRuntime: Boolean = true,
    val isADB: Boolean = false,
    val isOptional: Boolean = false,
    val isSpecial: Boolean = false
) {
    /**
     * Checks if the permission is granted.
     *
     * @param context The context to use for checking the permission.
     * @return True if the permission is granted, false otherwise.
     */
    fun isGranted(context: Context): Boolean {
        if (isSpecial) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                name,
                Process.myUid(), context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        } else if (isRuntime) {
            return ContextCompat.checkSelfPermission(
                context,
                name
            ) == PackageManager.PERMISSION_GRANTED
        } else return true
    }

    /**
     * Requests the permission.
     *
     * @param context The context to use for requesting the permission.
     * @param requestPermissionLauncher The launcher to use for requesting the permission.
     */
    fun askPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    ) {
        if (isSpecial) {
            // special permissions have to use the Codes from AppOpsManager
            // all other permissions from Manifest.permission
            if (name == OPSTR_GET_USAGE_STATS) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        } else if (isRuntime) {
            requestPermissionLauncher.launch(arrayOf(name))
        }
    }

    /**
     * Checks and requests the permission if not granted.
     *
     * @param context The context to use for checking and requesting the permission.
     * @param requestPermissionLauncher The launcher to use for requesting the permission.
     * @return True if the permission is granted, false otherwise.
     */
    fun checkPermission(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    ): Boolean {
        if (!isGranted(context)) {
            askPermission(context, requestPermissionLauncher)
            return false
        }
        return true
    }
}