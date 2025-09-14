package com.flxholle.forensiceye.data.sources

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import com.flxholle.forensiceye.data.KeyValueDataSource
import com.flxholle.forensiceye.data.PermissionWrapper


class BluetoothDataSource(context: Context) : KeyValueDataSource(context) {

    @SuppressLint("MissingPermission")
    override fun getContentMap(): Map<String, Any> {
        val bluetoothDevicesMap = mutableMapOf<String, Any>()
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val bondedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        for (device in bondedDevices) {
            val deviceName = device.name
            val deviceAddress = device.address
            val deviceType = device.type
            val deviceBondState = device.bondState
            val alias = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                device.alias
            } else {
                "unavailable"
            }
            val uuids = device.uuids
            val bluetoothClass = device.bluetoothClass

            val deviceMap = mapOf(
                "Name" to deviceName,
                "Type" to deviceType,
                "Bond State" to deviceBondState,
                "Alias" to alias,
                "UUIDs" to uuids,
                "Bluetooth Class" to bluetoothClass
            )

            bluetoothDevicesMap[deviceAddress] = deviceMap
        }
        return bluetoothDevicesMap
    }

    override fun getFilename(): String {
        return "bluetooth.json"
    }

    override fun getPermissions(): List<PermissionWrapper> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                PermissionWrapper(Manifest.permission.BLUETOOTH_CONNECT),
                PermissionWrapper(Manifest.permission.ACCESS_COARSE_LOCATION),
                PermissionWrapper(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        } else {
            listOf(
                PermissionWrapper(Manifest.permission.ACCESS_COARSE_LOCATION),
                PermissionWrapper(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    /**
     * Checks if the necessary permissions are granted and Bluetooth is enabled.
     * This is a special case for Android 12 (API level 31) and above, where Bluetooth permissions are not sufficient alone.
     *
     * @return True if permissions are granted and Bluetooth is enabled, false otherwise.
     */
    override fun permissionsGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return super.permissionsGranted() && bluetoothAdapter.isEnabled
        } else {
            return super.permissionsGranted()
        }
    }

    /**
     * Requests the necessary permissions for accessing Bluetooth data.
     * This is a special case for Android 12 (API level 31) and above, where Bluetooth permissions are not sufficient alone.
     *
     * @param requestPermissionLauncher The launcher for requesting permissions.
     */
    override fun askPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        super.askPermissions(requestPermissionLauncher)
        if (super.permissionsGranted() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }
}