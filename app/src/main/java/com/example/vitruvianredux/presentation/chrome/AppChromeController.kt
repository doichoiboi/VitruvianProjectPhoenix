package com.example.vitruvianredux.presentation.chrome

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TopBarAction(
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit
)

data class AppChromeState(
    val dynamicTitle: String? = null,
    val topBarActions: List<TopBarAction> = emptyList(),
    val backAction: (() -> Unit)? = null
)

class AppChromeController {
    private val _state = MutableStateFlow(AppChromeState())
    private var ownerKey: String? = null
    val state: StateFlow<AppChromeState> = _state.asStateFlow()

    fun setDynamicTitle(ownerKey: String, title: String) {
        claim(ownerKey)
        _state.value = _state.value.copy(dynamicTitle = title)
    }

    fun clearDynamicTitle(ownerKey: String) {
        if (!isOwner(ownerKey)) return
        _state.value = _state.value.copy(dynamicTitle = null)
    }

    fun setTopBarActions(ownerKey: String, actions: List<TopBarAction>) {
        claim(ownerKey)
        _state.value = _state.value.copy(topBarActions = actions)
    }

    fun clearTopBarActions(ownerKey: String) {
        if (!isOwner(ownerKey)) return
        _state.value = _state.value.copy(topBarActions = emptyList())
    }

    fun setBackAction(ownerKey: String, action: () -> Unit) {
        claim(ownerKey)
        _state.value = _state.value.copy(backAction = action)
    }

    fun clearBackAction(ownerKey: String) {
        if (!isOwner(ownerKey)) return
        _state.value = _state.value.copy(backAction = null)
    }

    fun clearChrome(ownerKey: String) {
        if (!isOwner(ownerKey)) return
        this.ownerKey = null
        _state.value = AppChromeState()
    }

    private fun claim(ownerKey: String) {
        this.ownerKey = ownerKey
    }

    private fun isOwner(ownerKey: String): Boolean =
        this.ownerKey == ownerKey
}

val LocalAppChrome = staticCompositionLocalOf<AppChromeController> {
    error("AppChromeController is not available")
}
