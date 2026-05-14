package com.example.vitruvianredux.presentation.chrome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vitruvianredux.domain.model.ConnectionState
import com.example.vitruvianredux.ui.theme.appStatusColors

enum class MachineConnectionTone {
    Success,
    Warning,
    Error,
    Info
}

data class MachineConnectionChromeState(
    val icon: ImageVector,
    val contentDescription: String,
    val label: String,
    val tone: MachineConnectionTone
) {
    companion object {
        fun from(connectionState: ConnectionState): MachineConnectionChromeState =
            when (connectionState) {
                is ConnectionState.Connected -> MachineConnectionChromeState(
                    icon = Icons.Default.Bluetooth,
                    contentDescription = "Connected to machine. Tap to disconnect",
                    label = "Connected",
                    tone = MachineConnectionTone.Success
                )
                is ConnectionState.Connecting -> MachineConnectionChromeState(
                    icon = Icons.AutoMirrored.Filled.BluetoothSearching,
                    contentDescription = "Connecting to machine",
                    label = "Connecting",
                    tone = MachineConnectionTone.Warning
                )
                is ConnectionState.Disconnected -> MachineConnectionChromeState(
                    icon = Icons.Default.BluetoothDisabled,
                    contentDescription = "Disconnected. Tap to connect",
                    label = "Disconnected",
                    tone = MachineConnectionTone.Error
                )
                is ConnectionState.Scanning -> MachineConnectionChromeState(
                    icon = Icons.AutoMirrored.Filled.BluetoothSearching,
                    contentDescription = "Scanning for machine",
                    label = "Scanning",
                    tone = MachineConnectionTone.Info
                )
                is ConnectionState.Error -> MachineConnectionChromeState(
                    icon = Icons.Default.BluetoothDisabled,
                    contentDescription = "Connection error. Tap to retry",
                    label = "Error",
                    tone = MachineConnectionTone.Error
                )
            }
    }
}

@Composable
fun MachineConnectionButton(
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chromeState = remember(connectionState) {
        MachineConnectionChromeState.from(connectionState)
    }
    val toneColor = when (chromeState.tone) {
        MachineConnectionTone.Success -> MaterialTheme.appStatusColors.success
        MachineConnectionTone.Warning -> MaterialTheme.appStatusColors.warning
        MachineConnectionTone.Error -> MaterialTheme.colorScheme.error
        MachineConnectionTone.Info -> MaterialTheme.appStatusColors.info
    }
    val onClick = if (connectionState is ConnectionState.Connected) onDisconnect else onConnect

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
    ) {
        Icon(
            imageVector = chromeState.icon,
            contentDescription = chromeState.contentDescription,
            tint = toneColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = chromeState.label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = toneColor,
            maxLines = 1
        )
    }
}
