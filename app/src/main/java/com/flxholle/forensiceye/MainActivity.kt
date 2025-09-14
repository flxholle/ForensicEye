package com.flxholle.forensiceye

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.documentfile.provider.DocumentFile
import com.flxholle.forensiceye.data.DataSource
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
import com.flxholle.forensiceye.ui.ActionIcon
import com.flxholle.forensiceye.ui.StatusColumn
import com.flxholle.forensiceye.ui.Text
import com.flxholle.forensiceye.ui.TitleBar
import com.flxholle.forensiceye.ui.theme.ForensicEyeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.DelicateCoroutinesApi
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import java.io.File
import java.io.FileInputStream

/*
███████████                                                 ███           ██████████
░░███░░░░░░█                                                ░░░           ░░███░░░░░█
 ░███   █ ░   ██████  ████████   ██████  ████████    █████  ████   ██████  ░███  █ ░  █████ ████  ██████
 ░███████    ███░░███░░███░░███ ███░░███░░███░░███  ███░░  ░░███  ███░░███ ░██████   ░░███ ░███  ███░░███
 ░███░░░█   ░███ ░███ ░███ ░░░ ░███████  ░███ ░███ ░░█████  ░███ ░███ ░░░  ░███░░█    ░███ ░███ ░███████
 ░███  ░    ░███ ░███ ░███     ░███░░░   ░███ ░███  ░░░░███ ░███ ░███  ███ ░███ ░   █ ░███ ░███ ░███░░░
 █████      ░░██████  █████    ░░██████  ████ █████ ██████  █████░░██████  ██████████ ░░███████ ░░██████
░░░░░        ░░░░░░  ░░░░░      ░░░░░░  ░░░░ ░░░░░ ░░░░░░  ░░░░░  ░░░░░░  ░░░░░░░░░░   ░░░░░███  ░░░░░░
                                                                                       ███ ░███
                                                                                      ░░██████
                                                                                       ░░░░░░
*/

/**
 * MainActivity class that handles the main UI and logic for the ForensicEye application.
 */
class MainActivity : ComponentActivity() {

    /**
     * Launcher for opening a document tree.
     */
    val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                copyDirectoryToUri(it)
            }
        }

    /**
     * Launcher for requesting multiple permissions.
     */
    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result -> }

    /**
     * Listener for Shizuku permission request results.
     */
    val shizukuRequestPermissionResultListener =
        OnRequestPermissionResultListener { requestCode: Int, grantResult: Int ->
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Toast.makeText(
                    this,
                    "Permission granted. Press again to start.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add Shizuku permission result listener
        Shizuku.addRequestPermissionResultListener(shizukuRequestPermissionResultListener)

        val context = this
        val dataSources1 = listOf(
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
            LastLocationDataSource(context)
        )

        val dataSources2 = listOf(
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

        enableEdgeToEdge()
        setContent {
            ForensicEyeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(8.dp),
                    ) {
                        var dataSourceOnClicks1: MutableState<List<() -> Unit>> =
                            remember { mutableStateOf(listOf({})) }
                        var dataSourceOnClicks2: MutableState<List<() -> Unit>> =
                            remember { mutableStateOf(listOf({})) }
                        var clicked = remember { mutableStateOf(false) }

                        // Set up the title bar with the app icon
                        TitleBar(
                            text1 = stringResource(R.string.forensic),
                            text2 = stringResource(R.string.eye),
                            appIcon = packageManager.getApplicationIcon(packageName)
                                .toBitmap(config = Bitmap.Config.ARGB_8888).asImageBitmap()
                        )
                        Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        DataDirectory()
                        Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        Button(
                            onClick = {
                                if (!clicked.value) {
                                    // If clicked for the first time, query all runtime permissions before invoking the data source button clicks
                                    val allRuntimePermissions = (dataSources1 + dataSources2)
                                        .flatMap {
                                            it.getPermissions()
                                                .filter { it.isRuntime && !it.isSpecial }
                                                .map { it.name }
                                        }
                                        .distinct()
                                    if (!allRuntimePermissions.isEmpty()) {
                                        requestPermissionLauncher.launch(allRuntimePermissions.toTypedArray())
                                    }
                                    clicked.value = true
                                } else {
                                    dataSourceOnClicks1.value.forEach { onClick -> onClick() }
                                    dataSourceOnClicks2.value.forEach { onClick -> onClick() }
                                }
                            },
                            modifier = Modifier
                                .padding(start = 8.dp, end = 8.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                if (!clicked.value) stringResource(R.string.query_all_permissions) else stringResource(
                                    R.string.run_all
                                )
                            )
                        }
                        if (Shizuku.pingBinder()) {
                            OutlinedButton(
                                onClick = {
                                    Thread {
                                        try {
                                            val allPermissions = (dataSources1 + dataSources2)
                                                .flatMap {
                                                    it.getPermissions()
                                                        .map { it.name }
                                                }
                                                .distinct()
                                            if (checkShizuku(0)) {
                                                for (permission in allPermissions) {
                                                    val cmd = arrayOf<String>(
                                                        "sh",
                                                        "-c",
                                                        "pm grant $packageName $permission",
                                                    )
                                                    val mProcess = Shizuku.newProcess(
                                                        cmd,
                                                        null,
                                                        "/"
                                                    ).apply {
                                                        errorStream.bufferedReader().use {
                                                            it.lines().forEach { line ->
                                                                Log.d(
                                                                    "ShizukuError",
                                                                    line
                                                                )
                                                            }
                                                        }
                                                    }
                                                    mProcess?.waitFor()
                                                    mProcess?.destroy()
                                                }
                                                runOnUiThread {
                                                    clicked.value = true
                                                    Toast.makeText(
                                                        context,
                                                        "Granted all permissions. It might be necessary to restart the app",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e(
                                                "ShizukuPermissions",
                                                "Error in granting permissions",
                                                e
                                            )
                                        }
                                    }.start()
                                }, modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .fillMaxWidth()
                            ) { Text(stringResource(R.string.grant_all_permissions_using_shizuku_experimental)) }
                        }
                        Spacer(modifier = Modifier.padding(bottom = 16.dp))
                        Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dataSourceOnClicks1.value = dataSources1.map { dataSource ->
                                    DataSourceUI(
                                        context,
                                        dataSource,
                                        dataSource::class.simpleName?.substringBefore("DataSource")
                                            ?: "Unknown"
                                    )
                                }.toList()
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dataSourceOnClicks2.value = dataSources2.map { dataSource ->
                                    DataSourceUI(
                                        context,
                                        dataSource,
                                        dataSource::class.simpleName?.substringBefore("DataSource")
                                            ?: "Unknown"
                                    )
                                }.toList()
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Called when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        // Remove Shizuku permission result listener
        Shizuku.removeRequestPermissionResultListener(shizukuRequestPermissionResultListener)
    }

    /**
     * Checks if Shizuku permission is granted.
     * @param code The request code for the permission.
     * @return True if the permission is granted, false otherwise.
     */
    private fun checkShizuku(code: Int): Boolean {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            return true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            return false
        } else {
            // Request the permission
            Shizuku.requestPermission(code)
            return false
        }
    }

    /**
     * Enum class representing the state of the data source.
     */
    enum class STATE {
        UNINIT,
        DISABLED,
        PERMISSIONS,
        START,
        RUNNING,
        SUCCESS,
        ERROR
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
    fun DataSourceUI(
        context: Context,
        dataSource: DataSource,
        name: String,
        modifier: Modifier = Modifier
    ): () -> Unit {
        val state = remember { mutableStateOf(STATE.UNINIT) }

        // Runtime permissions can be handled by JetpackCompose itself, however this destroys the clean abstraction :(
        val permissionStates =
            rememberMultiplePermissionsState(
                dataSource.getPermissions().filter { it.isRuntime && !it.isSpecial }
                    .map { it.name })

        var showDialog = remember { mutableStateOf(false) }
        var adbString = remember { mutableStateOf("") }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = stringResource(R.string.permission_needs_adb)) },
                text = {
                    Column {
                        Text(text = stringResource(R.string.some_permissions_can_only_be_granted_using_adb_however_they_might_be_optional_to_grant_them_please_execute_the_following_command))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "adb shell pm grant com.flxholle.forensiceye ${adbString.value}",
                            style = TextStyle(fontFamily = FontFamily.Monospace)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val clipboard =
                            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            getString(R.string.adb_command),
                            "adb shell pm grant com.flxholle.forensiceye ${adbString.value}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            getString(R.string.command_copied_to_clipboard), Toast.LENGTH_SHORT
                        )
                            .show()
                        showDialog.value = false
                    }) {
                        Text(stringResource(R.string.copy_to_clipboard))
                    }
                }
            )
        }

        val onClick = {
            when (state.value) {
                STATE.START, STATE.SUCCESS, STATE.ERROR -> {
                    if (!(permissionStates.allPermissionsGranted && dataSource.permissionsGranted())) {
                        state.value = STATE.PERMISSIONS
                    } else {
                        if (!dataSource.checkPermissions(requestPermissionLauncher)) {
                            state.value = STATE.PERMISSIONS
                        } else {
                            state.value = STATE.RUNNING
                            Thread {
                                val success = try {
                                    dataSource.writeToFile()
                                } catch (e: Exception) {
                                    Log.e("DataSource", "Error in writeToFile", e)
                                    false
                                }
                                runOnUiThread {
                                    state.value = if (success) STATE.SUCCESS else STATE.ERROR
                                }
                            }.start()
                        }
                    }
                }

                STATE.PERMISSIONS -> {
                    // Runtime permissions can be handled by JetpackCompose itself, but this destroys the abstraction
                    permissionStates.launchMultiplePermissionRequest()

                    // Special permissions and adb permissions cannot be handled by JetpackCompose, so special treatment is needed
                    val specialPermissions = dataSource.getPermissions().filter { it.isSpecial }
                    val adbPermissions = dataSource.getPermissions().filter { it.isADB }
                    if (specialPermissions.isNotEmpty() || adbPermissions.isNotEmpty()) {
                        for (permission in specialPermissions)
                            permission.askPermission(this, requestPermissionLauncher)

                        adbString.value = adbPermissions.joinToString(" ") { it.name }
                        if (adbPermissions.isNotEmpty())
                            showDialog.value = true

                        // Hacky solution to update the State, as these permissions are not supported by Jetpack Compose
                        Thread {
                            var counter = 0L
                            while (counter < 1000) {
                                Thread.sleep(100)
                                if (dataSource.permissionsGranted()) {
                                    runOnUiThread {
                                        state.value = STATE.START
                                    }
                                    break
                                }
                                counter += 1
                            }
                        }.start()
                    }
                }

                else -> {}
            }
        }

        Row(modifier = modifier) {
            if (state.value == STATE.UNINIT || state.value == STATE.PERMISSIONS) {
                state.value = if (!dataSource.enabled) {
                    STATE.DISABLED
                } else {
                    if (permissionStates.allPermissionsGranted && dataSource.permissionsGranted())
                        STATE.START
                    else
                        STATE.PERMISSIONS
                }
            }

            Button(
                onClick = onClick,
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                enabled = state.value != STATE.DISABLED
            ) {
                Text(
                    name,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 6.sp,
                        maxFontSize = 12.sp,
                        stepSize = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (state.value) {
                STATE.DISABLED, STATE.UNINIT -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_remove_circle_24,
                        tint = MaterialTheme.colorScheme.inversePrimary,
                        text = stringResource(R.string.unsupported),
                        modifier = Modifier.clickable {
                            Toast.makeText(
                                context,
                                getString(R.string.this_action_is_not_supported_on_this_device),
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        background = MaterialTheme.colorScheme.inverseSurface
                    )
                }

                STATE.START -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_play_circle_filled_24,
                        tint = MaterialTheme.colorScheme.primary,
                        text = stringResource(R.string.can_start)
                    )
                }

                STATE.PERMISSIONS -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_error_24,
                        tint = MaterialTheme.colorScheme.secondary,
                        text = stringResource(R.string.permission_needed)
                    )
                }

                STATE.RUNNING -> {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                }

                STATE.SUCCESS -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_check_circle_24,
                        tint = Color.Green,
                        text = stringResource(R.string.success),
                    )
                }

                STATE.ERROR -> {
                    StatusColumn(
                        iconId = R.drawable.baseline_dangerous_24,
                        tint = Color.Red,
                        text = stringResource(R.string.failed)
                    )
                }
            }
        }

        return onClick
    }

    /**
     * Composable function to display the data directory UI.
     * @param modifier The modifier to be applied to the layout.
     */
    @Composable
    fun DataDirectory(modifier: Modifier = Modifier) {
        val context = this
        val dataDirectory = getExternalFilesDir(null)?.absolutePath ?: "Error"

        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.data_directory),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    dataDirectory,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            ActionIcon(
                R.drawable.baseline_file_copy_24,
                stringResource(R.string.copy_data_directory),
                stringResource(R.string.copy),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable {
                        openDocumentTreeLauncher.launch(null)
                    }
            )
            ActionIcon(
                R.drawable.baseline_delete_forever_24,
                stringResource(R.string.delete_data_directory),
                stringResource(R.string.delete),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable {
                        val dir = File(dataDirectory)
                        if (dir.exists() && dir.isDirectory) {
                            dir.deleteRecursively()
                            Toast.makeText(
                                context,
                                getString(R.string.data_directory_cleared), Toast.LENGTH_SHORT
                            )
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                getString(R.string.data_directory_not_found), Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
            )
        }
    }

    /**
     * Copies the contents of the data directory to the specified URI.
     * @param uri The URI to copy the data to.
     */
    private fun copyDirectoryToUri(uri: Uri) {
        val context = this
        val dataDirectory = getExternalFilesDir(null) ?: return
        val documentFile = DocumentFile.fromTreeUri(context, uri) ?: return

        // Copy each file in the data directory to the target URI
        dataDirectory.listFiles()?.forEach { file ->
            val newFile =
                documentFile.createFile("application/octet-stream", file.name ?: "unknown")
            newFile?.let { copyFile(file, it) }
        }
    }

    /**
     * Copies a file to the specified target DocumentFile.
     * @param source The source file to copy.
     * @param target The target DocumentFile to copy to.
     */
    private fun copyFile(source: File, target: DocumentFile) {
        contentResolver.openOutputStream(target.uri)?.use { outputStream ->
            FileInputStream(source).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}

