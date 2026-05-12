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
    val state: StateFlow<AppChromeState> = _state.asStateFlow()

    fun setDynamicTitle(title: String) {
        _state.value = _state.value.copy(dynamicTitle = title)
    }

    fun clearDynamicTitle() {
        _state.value = _state.value.copy(dynamicTitle = null)
    }

    fun setTopBarActions(actions: List<TopBarAction>) {
        _state.value = _state.value.copy(topBarActions = actions)
    }

    fun clearTopBarActions() {
        _state.value = _state.value.copy(topBarActions = emptyList())
    }

    fun setBackAction(action: () -> Unit) {
        _state.value = _state.value.copy(backAction = action)
    }

    fun clearBackAction() {
        _state.value = _state.value.copy(backAction = null)
    }
}

val LocalAppChrome = staticCompositionLocalOf<AppChromeController> {
    error("AppChromeController is not available")
}
