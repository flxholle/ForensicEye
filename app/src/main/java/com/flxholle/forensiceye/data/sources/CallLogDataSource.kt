package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class CallLogDataSource(context: Context) : ContentProviderDataSource(context) {

    /**
     * Returns a list of URIs for the call log content provider.
     * The CallComposer locations URI is commented out because it requires the app to be the default dialer.
     * See BlockedNumbersDataSource for more information.
     *
     * @return List of URIs to access call log data.
     */
    override fun getURIs(): List<Uri> {
        return mutableListOf(
            CallLog.Calls.CONTENT_URI,
        )
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            //java.lang.SecurityException: Access to call composer locations is only allowed for the default dialer: com.google.android.dialer (see)
//            callLogsUris.add(CallLog.Locations.CONTENT_URI)
//        }
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.READ_CALL_LOG))
    }
}