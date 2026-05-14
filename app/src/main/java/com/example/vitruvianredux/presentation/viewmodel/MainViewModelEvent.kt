package com.example.vitruvianredux.presentation.viewmodel

import com.example.vitruvianredux.ui.theme.ThemeMode

sealed interface MainViewModelEvent {
    data class ThemeModeSelected(val mode: ThemeMode) : MainViewModelEvent
    data class DeviceConnectionRequested(val deviceAddress: String) : MainViewModelEvent
    object DisconnectRequested : MainViewModelEvent
    object AutoConnectCancelled : MainViewModelEvent
    object ConnectionErrorDismissed : MainViewModelEvent
    object ConnectionLostAlertDismissed : MainViewModelEvent
}
