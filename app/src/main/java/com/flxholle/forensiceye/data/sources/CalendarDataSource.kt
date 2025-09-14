package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class CalendarDataSource(context: Context) : ContentProviderDataSource(context) {
    override fun getURIs(): List<Uri> {
        return listOf(
            CalendarContract.Calendars.CONTENT_URI,
//            CalendarContract.Calendars.ENTERPRISE_CONTENT_URI,
            CalendarContract.Events.CONTENT_URI,
//            CalendarContract.Events.ENTERPRISE_CONTENT_URI,
//            CalendarContract.Instances.CONTENT_URI,
            CalendarContract.Attendees.CONTENT_URI,
            CalendarContract.Reminders.CONTENT_URI,
            CalendarContract.Colors.CONTENT_URI,
            CalendarContract.ExtendedProperties.CONTENT_URI,
            CalendarContract.SyncState.CONTENT_URI
        )
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.READ_CALENDAR))
    }
}