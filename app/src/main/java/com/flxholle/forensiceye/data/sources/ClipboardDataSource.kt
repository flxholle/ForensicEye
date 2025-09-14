package com.flxholle.forensiceye.data.sources

import android.content.ClipboardManager
import android.content.Context
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper

class ClipboardDataSource(context: Context) : KeyValueDataSource(context) {
    override fun getContentMap(): Map<String, Any> {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val retMap = mutableMapOf<String, Any>()
        if (clipboardManager.hasPrimaryClip()) {
            val lastClip = clipboardManager.primaryClip
            val lastClipDescription = clipboardManager.primaryClipDescription
            if (lastClip != null) {
                for (i in 0 until lastClip.itemCount) {
                    val item = lastClip.getItemAt(i)
                    retMap["clipItem_$i"] = item.text ?: "unavailable"
                }
            }
            retMap["descriptionLabel"] = lastClipDescription?.label ?: "unavailable"
        } else {
            retMap["noClip"] = "no clip available"
        }
        return retMap
    }

    override fun getFilename(): String {
        return "clipboard.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf()
    }
}