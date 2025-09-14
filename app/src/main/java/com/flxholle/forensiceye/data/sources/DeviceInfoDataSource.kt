package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import com.flxholle.forensiceye.data.DataSource
import com.flxholle.forensiceye.data.PermissionWrapper
import org.acra.ACRA

class DeviceInfoDataSource(context: Context) : DataSource(context, true) {

    /**
     * Writes device information to a file using a bugreport called with a custom exception using ACRA.
     *
     * @return Boolean indicating the success of the operation.
     */
    override fun writeToFileInternal(): Boolean {
        ACRA.errorReporter.handleSilentException(Exception("Report requested by developer"))
        return true
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(
            PermissionWrapper(
                Manifest.permission.DUMP,
                isRuntime = false,
                isADB = true,
                isOptional = false
            ),
            PermissionWrapper(
                Manifest.permission.READ_LOGS,
                isRuntime = false,
                isADB = true,
                isOptional = false
            )
        )
    }
}