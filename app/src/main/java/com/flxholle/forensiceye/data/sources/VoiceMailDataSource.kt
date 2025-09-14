package com.flxholle.forensiceye.data.sources

import android.content.Context
import android.net.Uri
import android.provider.VoicemailContract
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

//permission only to system app
class VoiceMailDataSource(context: Context) : ContentProviderDataSource(context, enabled = false) {
    override fun getURIs(): List<Uri> {
        return listOf(
            VoicemailContract.Voicemails.CONTENT_URI
        )
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(android.Manifest.permission.ADD_VOICEMAIL))
    }
}