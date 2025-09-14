package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class ContactsDataSource(context: Context) : ContentProviderDataSource(context) {
    override fun getURIs(): List<Uri> {
        return listOf(
            ContactsContract.Contacts.CONTENT_URI,
            ContactsContract.RawContacts.CONTENT_URI,
            ContactsContract.Data.CONTENT_URI,
            ContactsContract.DeletedContacts.CONTENT_URI,
            ContactsContract.Profile.CONTENT_URI
        )
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(
            PermissionWrapper(Manifest.permission.READ_CONTACTS)
        )
    }
}