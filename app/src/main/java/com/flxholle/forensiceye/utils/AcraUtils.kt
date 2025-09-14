package com.flxholle.forensiceye.utils

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.google.auto.service.AutoService
import org.acra.ReportField.STACK_TRACE
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderException
import org.acra.sender.ReportSenderFactory
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class AcraSender : ReportSender {

    /**
     * Sends the crash report data to a file.
     *
     * @param context The application context.
     * @param errorContent The crash report data.
     * @param extras Additional data.
     * @throws ReportSenderException If an error occurs while sending the report.
     */
    @Throws(ReportSenderException::class)
    override fun send(context: Context, errorContent: CrashReportData, extras: Bundle) {
        Log.d("ACRA-Logger", "Received error content")

        // Check if it's a developer report and not a real crash
        if (errorContent.getString(STACK_TRACE)
                ?.contains("Report requested by developer") != true
        ) {
            return
        }

        val externalFile = File(context.getExternalFilesDir(null), "device_info.json")
        val fileUri = Uri.fromFile(externalFile)

        try {
            // Write the crash report data to the file
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.append(errorContent.toJSON())
                }
            }
            Log.d("ACRA-Logger", "File created successfully at $fileUri")
        } catch (e: IOException) {
            Log.e("ACRA-Logger", "Error writing file at $fileUri", e)
        }
    }
}

/**
 * Factory for creating instances of AcraSender.
 */
@AutoService(ReportSenderFactory::class)
class AcraFactory : ReportSenderFactory {
    /**
     * Creates a new instance of AcraSender.
     *
     * @param context The application context.
     * @param config The ACRA core configuration.
     * @return A new instance of AcraSender.
     */
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return AcraSender()
    }
}