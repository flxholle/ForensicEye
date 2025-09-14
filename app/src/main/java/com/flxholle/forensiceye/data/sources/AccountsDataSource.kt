package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class AccountsDataSource(context: Context) : KeyValueDataSource(context, enabled = isAvailable()) {
    override fun getContentMap(): Map<String, Any> {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts = accountManager.accounts
        return mapOf(Pair("accounts", accounts))
    }

    override fun getFilename(): String {
        return "accounts.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.GET_ACCOUNTS))
    }

    companion object {
        /**
         * Checks if the data source is available based on the Android version.
         *
         * @return True if the Android version is less than or equal to Oreo (8.0), false otherwise.
         */
        private fun isAvailable(): Boolean {
            return Build.VERSION.SDK_INT <= Build.VERSION_CODES.O
        }
    }
}