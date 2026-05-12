package com.example.vitruvianredux.presentation.screen

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.presentation.chrome.AppChromeController
import com.example.vitruvianredux.presentation.chrome.LocalAppChrome
import com.example.vitruvianredux.presentation.chrome.MachineConnectionButton
import com.example.vitruvianredux.presentation.navigation.AppNavigationHub
import com.example.vitruvianredux.presentation.navigation.NavGraph
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.presentation.viewmodel.ScannedDevice
import com.example.vitruvianredux.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.example.vitruvianredux.presentation.viewmodel.ThemeViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    exerciseRepository: ExerciseRepository = viewModel.exerciseRepository
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val connectionLostDuringWorkout by viewModel.connectionLostDuringWorkout.collectAsState()
    val isAutoConnecting by viewModel.isAutoConnecting.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()

    val themeViewModel: ThemeViewModel = hiltViewModel()
    val themeMode by themeViewModel.themeMode.collectAsState()
    val appChrome = remember { AppChromeController() }
    val chromeState by appChrome.state.collectAsState()

    // Determine if we're in dark mode for TopAppBar color
    val isDarkMode = when (themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(NavigationRoutes.Home.route) }

    // Track navigation changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: NavigationRoutes.Home.route
        }
    }
    
    // Request BLE permissions
    // NOTE: Must be declared BEFORE shouldShowBottomBar which depends on permissionState
    // On Android 12+ (API 31+), location is NOT needed because manifest uses neverForLocation flag
    // On older Android, location IS required for BLE scanning
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    val permissionState = rememberMultiplePermissionsState(permissions)

    val isWorkoutsRoute = remember(currentRoute) {
        AppNavigationHub.isWorkoutSection(currentRoute)
    }

    // Determine if we should show the TopBar
    // Always show TopBar now (ActiveWorkout uses global header)
    val shouldShowTopBar = remember(currentRoute) {
        true
    }

    val appBarTitle = remember(currentRoute, chromeState.dynamicTitle) {
        AppNavigationHub.appBarTitle(currentRoute, chromeState.dynamicTitle)
    }

    // Determine if we should show the BottomBar
    // Show only for main tabs AND when permissions are granted (NavGraph exists)
    // Using derivedStateOf for proper reactivity when permission state changes
    val shouldShowBottomBar by remember {
        derivedStateOf {
            permissionState.allPermissionsGranted &&
                AppNavigationHub.isBottomBarDestination(currentRoute)
        }
    }

    val showBackButton = remember(currentRoute) {
        AppNavigationHub.showsBackButton(currentRoute)
    }

    CompositionLocalProvider(LocalAppChrome provides appChrome) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Let components handle their own insets
        topBar = {
            if (shouldShowTopBar) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(), // Handle status bar for edge-to-edge
                    title = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // Main title - either dynamic or default
                            Text(
                                text = appBarTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Subtitle - always show "Vitruvian Project Phoenix"
                            Text(
                                text = "Vitruvian Project Phoenix",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFF97316), // Orange
                                            Color(0xFFEF4444)  // Red
                                        )
                                    ),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = {
                                if (chromeState.backAction != null) {
                                    chromeState.backAction?.invoke()
                                } else {
                                    navController.navigateUp()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDarkMode) TopAppBarDark else TopAppBarLight,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                ),
                actions = {
                    // Dynamic Actions from Screens
                    chromeState.topBarActions.forEach { action ->
                        IconButton(onClick = action.onClick) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.description,
                                tint = TextPrimary
                            )
                        }
                    }

                    MachineConnectionButton(
                        connectionState = connectionState,
                        onConnect = {
                            viewModel.ensureConnection(
                                onConnected = {},
                                onFailed = {}
                            )
                        },
                        onDisconnect = { viewModel.disconnect() }
                    )

                    // Theme toggle
                    com.example.vitruvianredux.presentation.components.ThemeToggle(
                        mode = themeMode,
                        onModeChange = { themeViewModel.setThemeMode(it) }
                    )
                }
            )
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = if (isDarkMode) Color(0xFF1C1B1F) else Color(0xFFF3F3F3),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    // Analytics tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Analytics"
                            )
                        },
                        label = { Text("Analytics") },
                        selected = currentRoute == NavigationRoutes.Analytics.route,
                        onClick = {
                            if (currentRoute != NavigationRoutes.Analytics.route) {
                                navController.navigate(NavigationRoutes.Analytics.route) {
                                    popUpTo(NavigationRoutes.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Workouts tab (Home) - center
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Workouts"
                            )
                        },
                        label = { Text("Workouts") },
                        selected = isWorkoutsRoute,
                        onClick = {
                            if (currentRoute != NavigationRoutes.Home.route) {
                                navController.navigate(NavigationRoutes.Home.route) {
                                    popUpTo(NavigationRoutes.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Settings tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        selected = currentRoute == NavigationRoutes.Settings.route,
                        onClick = {
                            if (currentRoute != NavigationRoutes.Settings.route) {
                                navController.navigate(NavigationRoutes.Settings.route) {
                                    popUpTo(NavigationRoutes.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        // Use proper padding to account for TopAppBar and system bars (status bar, notch, etc.)
        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val adjustedPadding = PaddingValues(
            start = padding.calculateLeftPadding(layoutDirection),
            end = padding.calculateRightPadding(layoutDirection),
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding()
        )

        if (!permissionState.allPermissionsGranted) {
            PermissionRequestScreen(
                permissionState = permissionState,
                modifier = Modifier.padding(adjustedPadding)
            )
        } else {
            NavGraph(
                navController = navController,
                viewModel = viewModel,
                exerciseRepository = exerciseRepository,
                themeMode = themeMode,
                onThemeModeChange = { mode -> themeViewModel.setThemeMode(mode) },
                modifier = Modifier.padding(adjustedPadding)
            )
        }
    }

    // Show connection lost alert during workout (Issue #43)
    if (connectionLostDuringWorkout) {
        com.example.vitruvianredux.presentation.components.ConnectionLostDialog(
            onReconnect = {
                viewModel.dismissConnectionLostAlert()
                viewModel.ensureConnection(
                    onConnected = {},
                    onFailed = {}
                )
            },
            onDismiss = {
                viewModel.dismissConnectionLostAlert()
            }
        )
    }

    if (isAutoConnecting) {
        com.example.vitruvianredux.presentation.components.ConnectingOverlay(
            onCancel = { viewModel.cancelAutoConnecting() }
        )
    }

    connectionError?.let { error ->
        com.example.vitruvianredux.presentation.components.ConnectionErrorDialog(
            message = error,
            onDismiss = { viewModel.clearConnectionError() }
        )
    }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionState: MultiplePermissionsState,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Track if we've already attempted to request permissions
    var hasAttemptedRequest by remember { mutableStateOf(false) }

    // Check if any permission was permanently denied:
    // - We've attempted to request AND shouldShowRationale is false AND permissions still not granted
    val permanentlyDenied = hasAttemptedRequest &&
        permissionState.revokedPermissions.isNotEmpty() &&
        !permissionState.shouldShowRationale

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Permission information",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.medium))
        Text(
            "Bluetooth permissions required",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.small))
        Text(
            buildString {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    append("This app needs Bluetooth permissions to connect to your Vitruvian machine.")
                } else {
                    append("This app needs Bluetooth and Location permissions to connect to your Vitruvian machine.")
                }
                if (permanentlyDenied) {
                    append("\n\nSome permissions were denied. Please grant them in Settings.")
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Spacing.large))

        // Always show the Grant permissions button - it will request or show rationale
        Button(onClick = {
            hasAttemptedRequest = true
            permissionState.launchMultiplePermissionRequest()
        }) {
            Icon(Icons.Default.Check, contentDescription = "Confirm")
            Spacer(modifier = Modifier.width(Spacing.small))
            Text("Grant permissions")
        }

        // Also show Open Settings button if permissions were permanently denied
        if (permanentlyDenied) {
            Spacer(modifier = Modifier.height(Spacing.small))
            OutlinedButton(onClick = {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Open Settings")
            }
        }
    }
}

@Composable
fun DeviceSelectorDialog(
    devices: List<ScannedDevice>,
    isScanning: Boolean,
    onDeviceSelected: (ScannedDevice) -> Unit,
    onRescan: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Vitruvian Device") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (devices.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isScanning) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(Spacing.small))
                                Text("Scanning...")
                            }
                        } else {
                            Text(
                                "No devices found. Try scanning again.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (devices.isNotEmpty()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                        items(devices, key = { it.address }) { device ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDeviceSelected(device) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(20.dp) // Material 3 Expressive: More rounded (was 16dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.medium),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            device.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            device.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Navigate to device",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isScanning) {
                TextButton(onClick = onRescan) {
                    Icon(Icons.Default.Refresh, contentDescription = "Rescan for devices", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(Spacing.extraSmall))
                    Text("Rescan", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

