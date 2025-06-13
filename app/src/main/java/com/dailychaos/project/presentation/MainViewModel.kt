package com.dailychaos.project.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel - Global app state management
 *
 * "ViewModel utama untuk manage event global aplikasi - seperti guild master yang koordinasi semua quest!"
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    // TODO: Inject global use cases when needed
    // private val authRepository: AuthRepository,
    // private val syncManager: SyncManager
) : ViewModel() {

    private val _globalEvents = MutableSharedFlow<GlobalEvent>()
    val globalEvents = _globalEvents.asSharedFlow()

    /**
     * Handle user logout globally
     */
    fun userLoggedOut() {
        viewModelScope.launch {
            try {
                // TODO: Implement actual logout logic
                // authRepository.logout()
                // syncManager.stopSync()

                _globalEvents.emit(GlobalEvent.UserLoggedOut)
            } catch (e: Exception) {
                // Handle logout error
                _globalEvents.emit(GlobalEvent.Error("Logout failed: ${e.message}"))
            }
        }
    }

    /**
     * Handle post creation (generic for any content)
     */
    fun postCreated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostCreated(postId))
        }
    }

    /**
     * Handle post update
     */
    fun postUpdated(postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.PostUpdated(postId))
        }
    }

    /**
     * Handle chaos entry creation
     */
    fun chaosEntryCreated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryCreated(entryId))
        }
    }

    /**
     * Handle chaos entry update
     */
    fun chaosEntryUpdated(entryId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosEntryUpdated(entryId))
        }
    }

    /**
     * Handle community post shared
     */
    fun communityPostShared(entryId: String, postId: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.CommunityPostShared(entryId, postId))
        }
    }

    /**
     * Handle support given to community post
     */
    fun supportGiven(postId: String, supportType: String) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.SupportGiven(postId, supportType))
        }
    }

    /**
     * Handle chaos twins found
     */
    fun chaosTwinsFound(count: Int) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ChaosTwinsFound(count))
        }
    }

    /**
     * Handle app sync status
     */
    fun syncStatusChanged(isOnline: Boolean) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.SyncStatusChanged(isOnline))
        }
    }

    /**
     * Handle general app notifications
     */
    fun showNotification(message: String, type: NotificationType = NotificationType.INFO) {
        viewModelScope.launch {
            _globalEvents.emit(GlobalEvent.ShowNotification(message, type))
        }
    }
}

/**
 * Global events that can occur across the app
 */
sealed class GlobalEvent {
    object UserLoggedOut : GlobalEvent()
    data class PostCreated(val postId: String) : GlobalEvent()
    data class PostUpdated(val postId: String) : GlobalEvent()
    data class ChaosEntryCreated(val entryId: String) : GlobalEvent()
    data class ChaosEntryUpdated(val entryId: String) : GlobalEvent()
    data class CommunityPostShared(val entryId: String, val postId: String) : GlobalEvent()
    data class SupportGiven(val postId: String, val supportType: String) : GlobalEvent()
    data class ChaosTwinsFound(val count: Int) : GlobalEvent()
    data class SyncStatusChanged(val isOnline: Boolean) : GlobalEvent()
    data class ShowNotification(val message: String, val type: NotificationType) : GlobalEvent()
    data class Error(val message: String) : GlobalEvent()
}

/**
 * Notification types for different contexts
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    CHAOS_QUOTE // Special untuk KonoSuba quotes
}