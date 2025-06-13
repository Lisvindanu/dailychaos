package com.dailychaos.project.util

/**
 * Navigation Event - Event untuk navigasi
 */
sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
    data class NavigateTo(val route: String) : NavigationEvent()
    data class NavigateWithArgs(val route: String, val args: Map<String, Any> = emptyMap()) : NavigationEvent()
}