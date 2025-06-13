package com.dailychaos.project.presentation.ui.screen.community.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailychaos.project.domain.model.CommunityPost
import com.dailychaos.project.util.generateAnonymousUsername
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

@HiltViewModel
class CommunityFeedViewModel @Inject constructor(
    // private val getCommunityPostsUseCase: GetCommunityPostsUseCase,
    // private val giveSupportUseCase: GiveSupportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed(isInitialLoad = true)
    }

    fun onEvent(event: CommunityFeedEvent) {
        when (event) {
            is CommunityFeedEvent.Refresh -> loadFeed(isRefreshing = true)
            is CommunityFeedEvent.Retry -> loadFeed(isInitialLoad = true)
            is CommunityFeedEvent.GiveSupport -> giveSupport(event.postId, event.type)
            is CommunityFeedEvent.ReportPost -> reportPost(event.postId)
        }
    }

    private fun loadFeed(isInitialLoad: Boolean = false, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad) _uiState.update { it.copy(isLoading = true, error = null) }
            if (isRefreshing) _uiState.update { it.copy(isRefreshing = true, error = null) }

            // Mock Data
            kotlinx.coroutines.delay(1500)
            _uiState.update {
                it.copy(
                    posts = createMockPosts(),
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    private fun giveSupport(postId: String, type: com.dailychaos.project.domain.model.SupportType) {
        // Optimistic UI update
        _uiState.update { state ->
            val updatedPosts = state.posts.map { post ->
                if (post.id == postId) {
                    post.copy(supportCount = post.supportCount + 1)
                } else {
                    post
                }
            }
            state.copy(posts = updatedPosts)
        }
        // In real app, call use case and handle success/failure
    }

    private fun reportPost(postId: String) {
        // Handle report logic
    }

    private fun createMockPosts(): List<CommunityPost> {
        return List(10) { i ->
            CommunityPost(
                id = "post_$i",
                chaosEntryId = "entry_$i",
                anonymousUsername = String.generateAnonymousUsername(), // CORRECTED
                title = "A Day of Utter Chaos",
                description = "My quest today involved debugging a legacy system. It was like fighting a slime king... endless, messy, and I'm not sure if I won.",
                chaosLevel = Random.nextInt(5, 11),
                tags = listOf("work", "coding", "despair"),
                supportCount = Random.nextInt(0, 50),
                twinCount = Random.nextInt(0, 5),
                createdAt = Clock.System.now().minus(i.hours)
            )
        }
    }
}