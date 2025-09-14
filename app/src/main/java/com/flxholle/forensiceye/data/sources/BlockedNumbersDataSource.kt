package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.BlockedNumberContract
import com.flxholle.forensiceye.data.ContentProviderDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

/**
 * Data source for accessing blocked numbers.
 *
 * This class extends ContentProviderDataSource and provides URIs and permissions
 * required to access blocked numbers from the BlockedNumberContract.
 * Therefore, the app must be the default dialer or SMS app to access this data.
 * This is currently not implemented yet, thus the enabled flag is set to false.
 *
 * @param context The context of the caller.
 */
class BlockedNumbersDataSource(context: Context) :
    ContentProviderDataSource(context, enabled = false) {
    override fun getURIs(): List<Uri> {
        return listOf(
            BlockedNumberContract.BlockedNumbers.CONTENT_URI
        )
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.READ_SMS))
    }
}