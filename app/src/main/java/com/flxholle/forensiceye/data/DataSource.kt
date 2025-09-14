package com.flxholle.forensiceye.data

import android.content.Context
import androidx.activity.result.ActivityResultLauncher

/**
 * Abstract class representing a data source.
 *
 * @property context The context in which the data source operates.
 * @property enabled Indicates whether the data source is enabled.
 */
abstract class DataSource(val context: Context, val enabled: Boolean) {

    /**
     * Writes data to a file if the data source is enabled.
     *
     * @return `true` if the data was successfully written, `false` otherwise.
     */
    fun writeToFile(): Boolean {
        if (!enabled) return false
        return writeToFileInternal()
    }

    /**
     * Writes data to a file. This method must be implemented by subclasses.
     *
     * @return `true` if the data was successfully written, `false` otherwise.
     */
    abstract fun writeToFileInternal(): Boolean

    /**
     * Retrieves the list of permissions required by the data source.
     *
     * @return A list of `PermissionWrapper` objects representing the required permissions.
     */
    abstract fun getPermissions(): List<PermissionWrapper>

    /**
     * Checks if all required permissions are granted.
     *
     * @return `true` if all required permissions are granted, `false` otherwise.
     */
    open fun permissionsGranted(): Boolean {
        if (!enabled) return false

        var allGranted = true
        for (permission in getPermissions()) {
            allGranted = allGranted && (permission.isGranted(context) || permission.isOptional)
        }
        return allGranted
    }

    /**
     * Requests the necessary permissions if they are not already granted.
     *
     * @param requestPermissionLauncher The launcher to request permissions.
     */
    open fun askPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (!enabled) return

        // Request multiple runtime permissions at once
        val permissions: MutableList<String> = mutableListOf()

        for (permission in getPermissions()) {
            if (!permission.isGranted(context)) {
                if (permission.isRuntime && !permission.isSpecial)
                    permissions.add(permission.name)
                else
                    permission.askPermission(context, requestPermissionLauncher)
            }
        }
        if (!permissions.isEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    /**
     * Checks if all required permissions are granted and requests them if not.
     *
     * @param requestPermissionLauncher The launcher to request permissions.
     * @return `true` if all required permissions are granted, `false` otherwise.
     */
    open fun checkPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>): Boolean {
        if (!enabled) return false

        if (!permissionsGranted()) {
            askPermissions(requestPermissionLauncher)
            return false
        }
        return true
    }
}