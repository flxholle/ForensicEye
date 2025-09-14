package com.flxholle.forensiceye.data.sources

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class ProcessesDataSource(context: Context) : KeyValueDataSource(context, enabled = isAvailable()) {
    override fun getContentMap(): Map<String, Any> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        val processesData = mutableMapOf<String, Any>()

        for (process in runningProcesses) {
            processesData[process.pid.toString()] = mapOf(
                "pid" to process.pid,
                "uid" to process.uid,
                "memoryUsage" to activityManager.getProcessMemoryInfo(intArrayOf(process.pid))[0].totalPrivateDirty * 1024L,
                "packageList" to process.pkgList.toList(),
                "importance" to process.importance,
                "processName" to process.processName
            )
        }

        return processesData
    }

    override fun getFilename(): String {
        return "running_processes.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return emptyList()
    }

    companion object {
        /**
         * Checks if the data source is available based on the Android version, as the API only works until Android Q.
         *
         * @return True if the Android version is Q or below, false otherwise.
         */
        private fun isAvailable(): Boolean {
            return Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
        }
    }
}