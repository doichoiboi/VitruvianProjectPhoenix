package com.example.vitruvianredux.presentation.permissions

import android.Manifest
import android.os.Build

object BlePermissionPolicy {
    fun requiredPermissions(sdkInt: Int = Build.VERSION.SDK_INT): List<String> =
        if (sdkInt >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
}
