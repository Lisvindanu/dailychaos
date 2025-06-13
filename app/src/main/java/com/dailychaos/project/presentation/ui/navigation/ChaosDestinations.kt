package com.dailychaos.project.presentation.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Destinations for navigation in Daily Chaos application
 *
 * "Peta destinasi untuk petualangan chaos kita!"
 */
object ChaosDestinations {
    // Main Routes
    const val SPLASH_ROUTE = "splash"
    const val ONBOARDING_ROUTE = "onboarding"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    const val JOURNAL_ROUTE = "journal"
    const val COMMUNITY_ROUTE = "community"
    const val CREATE_CHAOS_ROUTE = "create_chaos"
    const val CHAOS_DETAIL_ROUTE = "chaos_detail"
    const val EDIT_CHAOS_ROUTE = "edit_chaos"
    const val CHAOS_TWINS_ROUTE = "chaos_twins"
    const val SUPPORT_ROUTE = "support"

    // Routes with parameters
    const val CHAOS_DETAIL_WITH_ID = "$CHAOS_DETAIL_ROUTE/{${Args.ENTRY_ID}}"
    const val EDIT_CHAOS_WITH_ID = "$EDIT_CHAOS_ROUTE/{${Args.ENTRY_ID}}"
    const val SUPPORT_WITH_POST_ID = "$SUPPORT_ROUTE/{${Args.POST_ID}}"

    // Helper functions to create routes with parameters
    fun chaosDetailRoute(entryId: String) = "$CHAOS_DETAIL_ROUTE/$entryId"
    fun editChaosRoute(entryId: String) = "$EDIT_CHAOS_ROUTE/$entryId"
    fun supportRoute(postId: String) = "$SUPPORT_ROUTE/$postId"

    // Auth-specific nested routes
    object Auth {
        const val LOGIN = LOGIN_ROUTE
        const val REGISTER = REGISTER_ROUTE
        const val ONBOARDING = ONBOARDING_ROUTE
    }

    // Main app nested routes
    object Main {
        const val HOME = HOME_ROUTE
        const val JOURNAL = JOURNAL_ROUTE
        const val COMMUNITY = COMMUNITY_ROUTE
        const val PROFILE = PROFILE_ROUTE
    }

    // Navigation arguments
    object Args {
        const val ENTRY_ID = "entryId"
        const val POST_ID = "postId"
        const val USER_ID = "userId"
        const val AUTHOR_NAME = "authorName"
    }
}