package com.flxholle.forensiceye.utils

import android.app.Application
import android.content.Context
import org.acra.BuildConfig
import org.acra.ReportField.ANDROID_VERSION
import org.acra.ReportField.APPLICATION_LOG
import org.acra.ReportField.APP_VERSION_CODE
import org.acra.ReportField.APP_VERSION_NAME
import org.acra.ReportField.AVAILABLE_MEM_SIZE
import org.acra.ReportField.BRAND
import org.acra.ReportField.BUILD
import org.acra.ReportField.BUILD_CONFIG
import org.acra.ReportField.CRASH_CONFIGURATION
import org.acra.ReportField.CUSTOM_DATA
import org.acra.ReportField.DEVICE_FEATURES
import org.acra.ReportField.DEVICE_ID
import org.acra.ReportField.DISPLAY
import org.acra.ReportField.DROPBOX
import org.acra.ReportField.DUMPSYS_MEMINFO
import org.acra.ReportField.ENVIRONMENT
import org.acra.ReportField.EVENTSLOG
import org.acra.ReportField.FILE_PATH
import org.acra.ReportField.INITIAL_CONFIGURATION
import org.acra.ReportField.INSTALLATION_ID
import org.acra.ReportField.IS_SILENT
import org.acra.ReportField.LOGCAT
import org.acra.ReportField.MEDIA_CODEC_LIST
import org.acra.ReportField.PACKAGE_NAME
import org.acra.ReportField.PHONE_MODEL
import org.acra.ReportField.PRODUCT
import org.acra.ReportField.RADIOLOG
import org.acra.ReportField.REPORT_ID
import org.acra.ReportField.SETTINGS_GLOBAL
import org.acra.ReportField.SETTINGS_SECURE
import org.acra.ReportField.SETTINGS_SYSTEM
import org.acra.ReportField.SHARED_PREFERENCES
import org.acra.ReportField.STACK_TRACE
import org.acra.ReportField.STACK_TRACE_HASH
import org.acra.ReportField.THREAD_DETAILS
import org.acra.ReportField.TOTAL_MEM_SIZE
import org.acra.ReportField.USER_APP_START_DATE
import org.acra.ReportField.USER_COMMENT
import org.acra.ReportField.USER_CRASH_DATE
import org.acra.ReportField.USER_EMAIL
import org.acra.ReportField.USER_IP
import org.acra.data.StringFormat
import org.acra.ktx.initAcra

/**
 * Custom Application class for initializing ACRA (Application Crash Reports for Android).
 */
class AcraApplication : Application() {

    /**
     * Attaches the base context and initializes ACRA with the specified configuration.
     *
     * @param base The base context to attach.
     */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        // Initialize ACRA with the specified configuration
        initAcra {
            // Core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON
            reportContent = listOf(
                REPORT_ID,
                APP_VERSION_CODE,
                APP_VERSION_NAME,
                PACKAGE_NAME,
                FILE_PATH,
                PHONE_MODEL,
                ANDROID_VERSION,
                BUILD,
                BRAND,
                PRODUCT,
                TOTAL_MEM_SIZE,
                AVAILABLE_MEM_SIZE,
                BUILD_CONFIG,
                CUSTOM_DATA,
                STACK_TRACE,
                STACK_TRACE_HASH,
                INITIAL_CONFIGURATION,
                CRASH_CONFIGURATION,
                DISPLAY,
                USER_COMMENT,
                USER_APP_START_DATE,
                USER_CRASH_DATE,
                DUMPSYS_MEMINFO,
                DROPBOX,
                LOGCAT,
                EVENTSLOG,
                RADIOLOG,
                IS_SILENT,
                DEVICE_ID,
                INSTALLATION_ID,
                USER_EMAIL,
                DEVICE_FEATURES,
                ENVIRONMENT,
                SETTINGS_SYSTEM,
                SETTINGS_SECURE,
                SETTINGS_GLOBAL,
                SHARED_PREFERENCES,
                APPLICATION_LOG,
                MEDIA_CODEC_LIST,
                THREAD_DETAILS,
                USER_IP
            )
        }
    }
}