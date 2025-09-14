package com.flxholle.forensiceye.data.sources

import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class SettingsDataSource(context: Context) : ContentProviderDataSource(context) {
    override fun getURIs(): List<Uri> {
        return listOf(
            Settings.Global.CONTENT_URI,
            Settings.System.CONTENT_URI,
            Settings.Secure.CONTENT_URI
        )

    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf()
    }
}