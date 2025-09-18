package com.flxholle.forensiceye

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.flxholle.forensiceye.MainActivity.STATE
import com.flxholle.forensiceye.data.sources.AccountsDataSource
import com.flxholle.forensiceye.data.sources.BlockedNumbersDataSource
import com.flxholle.forensiceye.data.sources.BluetoothDataSource
import com.flxholle.forensiceye.data.sources.CalendarDataSource
import com.flxholle.forensiceye.data.sources.CallLogDataSource
import com.flxholle.forensiceye.data.sources.ClipboardDataSource
import com.flxholle.forensiceye.data.sources.ContactsDataSource
import com.flxholle.forensiceye.data.sources.DeviceInfoDataSource
import com.flxholle.forensiceye.data.sources.DumpsysDataSource
import com.flxholle.forensiceye.data.sources.HealthDataSource
import com.flxholle.forensiceye.data.sources.LastLocationDataSource
import com.flxholle.forensiceye.data.sources.NetworkStatsDataSource
import com.flxholle.forensiceye.data.sources.PackageListDataSource
import com.flxholle.forensiceye.data.sources.ProcessesDataSource
import com.flxholle.forensiceye.data.sources.SMSDataSource
import com.flxholle.forensiceye.data.sources.SettingsDataSource
import com.flxholle.forensiceye.data.sources.StorageStatsDataSource
import com.flxholle.forensiceye.data.sources.TelephonyDataSource
import com.flxholle.forensiceye.data.sources.VoiceMailDataSource
import com.flxholle.forensiceye.data.sources.WifiNetworksDataSource
import com.flxholle.forensiceye.ui.StatusColumn
import com.flxholle.forensiceye.ui.Text
import com.flxholle.forensiceye.ui.TitleBar
import com.flxholle.forensiceye.ui.theme.ForensicEyeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

class AutoRunActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val autoRunExtra: String? = intent.getStringExtra("auto_run")
//        if (autoRunExtra != null) {
//            Log.d("MainActivity", "Received Auto Run Command ($autoRunExtra)")
//        }

        val autoRun = true
//            if (autoRunExtra != null && autoRunExtra.lowercase() == "true") true else false


        val context = this
        val dataSources = listOf(
            AccountsDataSource(context),
            BlockedNumbersDataSource(context),
            BluetoothDataSource(context),
            CalendarDataSource(context),
            CallLogDataSource(context),
            ClipboardDataSource(context),
            ContactsDataSource(context),
            DeviceInfoDataSource(context),
            DumpsysDataSource(context),
            HealthDataSource(context),
            LastLocationDataSource(context),
            NetworkStatsDataSource(context),
            PackageListDataSource(context),
            ProcessesDataSource(context),
            SettingsDataSource(context),
            SMSDataSource(context),
            StorageStatsDataSource(context),
            TelephonyDataSource(context),
            VoiceMailDataSource(context),
            WifiNetworksDataSource(context)
        )

        val dataDirectory = getExternalFilesDir(null)?.absolutePath ?: "Error"

        val dir = File(dataDirectory)
        if (dir.exists() && dir.isDirectory) {
            dir.deleteRecursively()
        }

        enableEdgeToEdge()
        setContent {
            ForensicEyeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp)
                    ) {
                        TitleBar(
                            text1 = "ForensicEye",
                            text2 = "Auto Run",
                            appIcon = packageManager.getApplicationIcon(packageName)
                                .toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap()
                        )
                        Spacer(modifier = Modifier.padding(bottom = 16.dp))

                        Column {
                            Text(
                                stringResource(R.string.data_directory),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                dataDirectory,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(modifier = Modifier.padding(bottom = 32.dp))

                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            //TODO Multithreaded adding of threads to the list causes issues with joining them later
                            val threads = mutableListOf<Thread>()

                            for (i in 0..dataSources.size / 3) {
                                Row(Modifier.fillMaxWidth()) {
                                    for (j in 0..2) {
                                        val index = i * 3 + j
                                        if (index < dataSources.size) {

                                            // Handle each data source
                                            val dataSource = dataSources[index]
                                            val state = remember {
                                                mutableStateOf(
                                                    if (!dataSource.enabled) {
                                                        STATE.DISABLED
                                                    } else {
                                                        if (dataSource.permissionsGranted())
                                                            STATE.START
                                                        else
                                                            STATE.PERMISSIONS
                                                    }
                                                )
                                            }

                                            //Auto run the data source if it is in the START state
                                            if (state.value == STATE.START) {
                                                state.value = STATE.RUNNING
                                                val thread = Thread {
                                                    val success = try {
                                                        dataSource.writeToFile()
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            "DataSource",
                                                            "Error in writeToFile",
                                                            e
                                                        )
                                                        false
                                                    }
                                                    runOnUiThread {
                                                        state.value =
                                                            if (success) STATE.SUCCESS else STATE.ERROR
                                                    }
                                                }
                                                threads.add(thread)
                                            }

                                            DataSourceUIAutoRun(
                                                context,
                                                state.value,
                                                dataSources[index]::class.simpleName?.substringBefore(
                                                    "DataSource"
                                                )
                                                    ?: "Unknown",
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(4.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            LaunchedEffect(0) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    for (t in threads) {
                                        t.start()
                                    }
                                    for (t in threads) {
                                        t.join()
                                    }
                                    val externalFile =
                                        File(
                                            context.getExternalFilesDir(null),
                                            "finished_auto_run.txt"
                                        )
                                    val fileUri = Uri.fromFile(externalFile)
                                    try {
                                        context.contentResolver.openOutputStream(fileUri)
                                            ?.use { outputStream ->
                                                OutputStreamWriter(outputStream).use { writer ->
                                                    writer.append("Auto Run Finished")
                                                }
                                            }
                                        Log.d(
                                            "AutoRunActivity",
                                            "Auto Run Finished file created successfully at $fileUri"
                                        )
                                    } catch (e: IOException) {
                                        Log.e(
                                            "AutoRunActivity",
                                            "Error writing Auto Run Finished file at $fileUri",
                                            e
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Composable function to display the UI for a data source.
     * @param context The context of the activity.
     * @param dataSource The data source to display.
     * @param name The name of the data source.
     * @param modifier The modifier to be applied to the layout.
     * @return A lambda function to be executed on click.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun DataSourceUIAutoRun(
        context: Context,
        state: STATE,
        name: String,
        modifier: Modifier = Modifier
    ) {
        Row(modifier = modifier) {
            Text(
                name,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 6.sp,
                    maxFontSize = 14.sp,
                    stepSize = 2.sp
                ),
                modifier = Modifier.weight(100f),
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            when (state) {
                STATE.DISABLED, STATE.UNINIT -> {
                    StatusColumn(
                        iconId = R.drawable.outline_remove_circle_outline_24,
                        tint = MaterialTheme.colorScheme.inversePrimary,
                        text = "",
                        background = MaterialTheme.colorScheme.inverseSurface
                    )
                }

                STATE.START -> {
                    StatusColumn(
                        iconId = R.drawable.outline_check_circle_outline_24,
                        tint = MaterialTheme.colorScheme.primary,
                        text = ""
                    )
                }

                STATE.PERMISSIONS -> {
                    StatusColumn(
                        iconId = R.drawable.outline_error_24,
                        tint = MaterialTheme.colorScheme.secondary,
                        text = ""
                    )
                }

                STATE.RUNNING -> {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }

                STATE.SUCCESS -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_check_circle_24,
                        tint = Color.Green,
                        text = "",
                    )
                }

                STATE.ERROR -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_dangerous_24,
                        tint = Color.Red,
                        text = ""
                    )
                }
            }
        }
    }
}