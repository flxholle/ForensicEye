package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import com.flxholle.forensiceye.data.DataSource
import com.flxholle.forensiceye.data.PermissionWrapper
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * A data source that captures and writes the output of the `dumpsys` command to a file.
 *
 * @param context The context used to access system services.
 */
class DumpsysDataSource(context: Context) : DataSource(context, enabled = true) {

    /**
     * Writes the output of the `dumpsys` command to a file in the external files directory.
     *
     * @return `true` if the file was written successfully, `false` otherwise.
     */
    override fun writeToFileInternal(): Boolean {
        val dumpsysOutput = getDumpsysOutput()
        if (dumpsysOutput.isEmpty()) {
            Log.e("DumpsysDataSource", "No dumpsys output to write")
            return false
        }

        val externalFile = File(context.getExternalFilesDir(null), "dumpsys.txt")
        val fileUri = Uri.fromFile(externalFile)
        try {
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(dumpsysOutput)
                }
            }
            Log.d("DumpsysDataSource", "File created successfully at ${externalFile.absolutePath}")
        } catch (e: IOException) {
            Log.e("DumpsysDataSource", "Error writing file at ${externalFile.absolutePath}", e)
            return false
        }
        return true
    }

    /**
     * Executes the `dumpsys` command and captures its output.
     *
     * @return The output of the `dumpsys` command, or an empty string if an error occurred.
     */
    private fun getDumpsysOutput(): String {
        return try {
            val process = Runtime.getRuntime().exec("dumpsys")
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e("DumpsysDataSource", "Error executing dumpsys command", e)
            ""
        }
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return listOf(PermissionWrapper(Manifest.permission.DUMP, isADB = true))
    }
}