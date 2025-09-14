package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class SMSDataSource(context: Context) : ContentProviderDataSource(context) {
    override fun getURIs(): List<Uri> {
        val res = mutableListOf(
            Telephony.Sms.CONTENT_URI,
            Telephony.Sms.Draft.CONTENT_URI,
            Telephony.Sms.Conversations.CONTENT_URI,
            Telephony.Sms.Inbox.CONTENT_URI,
            Telephony.Sms.Outbox.CONTENT_URI,
            Telephony.Sms.Sent.CONTENT_URI,
            Telephony.Mms.CONTENT_URI,
            Telephony.Mms.Draft.CONTENT_URI,
            Telephony.Mms.Inbox.CONTENT_URI,
            Telephony.Mms.Outbox.CONTENT_URI,
            Telephony.Mms.Rate.CONTENT_URI,
            Telephony.Mms.Sent.CONTENT_URI,
            Telephony.ServiceStateTable.CONTENT_URI,
            //java.lang.SecurityException: No permission to access APN settings, could not figure out which permission is missing, maybe it is only available to system apps
//            Telephony.Carriers.CONTENT_URI,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            res.add(Telephony.Mms.Part.CONTENT_URI)
        }
        return res
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.READ_SMS))
    }
}