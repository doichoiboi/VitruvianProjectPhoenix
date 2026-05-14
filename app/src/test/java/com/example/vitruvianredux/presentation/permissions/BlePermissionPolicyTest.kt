package com.example.vitruvianredux.presentation.permissions

import android.Manifest
import android.os.Build
import kotlin.test.Test
import kotlin.test.assertEquals

class BlePermissionPolicyTest {
    @Test
    fun `android 12 and newer requires bluetooth scan and connect permissions`() {
        assertEquals(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            ),
            BlePermissionPolicy.requiredPermissions(Build.VERSION_CODES.S)
        )
    }

    @Test
    fun `pre android 12 requires legacy bluetooth and location permissions`() {
        assertEquals(
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            BlePermissionPolicy.requiredPermissions(Build.VERSION_CODES.R)
        )
    }
}
