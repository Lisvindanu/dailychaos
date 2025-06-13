package com.dailychaos.project.util

/**
 * Daily Chaos Constants
 *
 * "Bahkan di tengah chaos, kita butuh beberapa konstanta untuk pegangan"
 */
object Constants {

    // Database
    const val DATABASE_NAME = "daily_chaos_db"
    const val DATABASE_VERSION = 1

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHAOS_ENTRIES = "chaos_entries"
    const val COLLECTION_COMMUNITY_POSTS = "community_posts"
    const val COLLECTION_SUPPORT_REACTIONS = "support_reactions"
    const val COLLECTION_REPORTS = "reports"

    // Preferences
    const val PREF_USER_SETTINGS = "user_settings"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_ANONYMOUS_MODE = "anonymous_mode"

    // Chaos Levels
    const val CHAOS_LEVEL_MIN = 1
    const val CHAOS_LEVEL_MAX = 10
    const val CHAOS_LEVEL_DEFAULT = 5

    // Community
    const val MAX_CHAOS_ENTRY_LENGTH = 1000
    const val MAX_COMMUNITY_POST_LENGTH = 500
    const val MIN_SUPPORT_REACTION_COOLDOWN = 1000L // 1 second
    const val MAX_REPORTS_PER_USER_PER_DAY = 10

    // Sync
    const val SYNC_INTERVAL_MINUTES = 30L
    const val OFFLINE_CACHE_MAX_AGE_HOURS = 24

    // KonoSuba References
    const val KONOSUBA_QUOTES_REFRESH_INTERVAL = 60000L // 1 minute
    const val EXPLOSION_ANIMATION_DURATION = 1000L

    // Anonymous usernames prefixes (KonoSuba inspired)
    val ANONYMOUS_PREFIXES = listOf(
        "Kazuma", "Aqua", "Megumin", "Darkness", "Wiz", "Yunyun",
        "Chris", "Eris", "Chomusuke", "Vanir", "Adventurer", "Crusader",
        "Archwizard", "Priest", "Thief", "Knight", "Rookie", "Veteran"
    )

    // Error Messages
    const val ERROR_NETWORK_UNAVAILABLE = "Jaringan tidak tersedia. Quest akan dilanjutkan secara offline!"
    const val ERROR_SYNC_FAILED = "Sync gagal. Data lokal tetap aman!"
    const val ERROR_UNKNOWN = "Terjadi kesalahan tak terduga. Bahkan Aqua pun bingung!"

    // Success Messages
    const val SUCCESS_CHAOS_SAVED = "Chaos entry berhasil disimpan! 🌪️"
    const val SUCCESS_SUPPORT_GIVEN = "Dukungan berhasil diberikan! 💙"
    const val SUCCESS_SHARED_TO_COMMUNITY = "Dibagikan ke komunitas! Semoga ada yang relate 🤝"

    // Validation
    const val MIN_CHAOS_ENTRY_LENGTH = 10
    const val MIN_EMAIL_LENGTH = 5
    const val MIN_PASSWORD_LENGTH = 6

    // Notification Channels
    const val NOTIFICATION_CHANNEL_GENERAL = "general"
    const val NOTIFICATION_CHANNEL_SUPPORT = "support"
    const val NOTIFICATION_CHANNEL_SYNC = "sync"

    // Intent Actions
    const val ACTION_SHOW_CHAOS_ENTRY = "com.dailychaos.project.SHOW_CHAOS_ENTRY"
    const val ACTION_SHOW_COMMUNITY_POST = "com.dailychaos.project.SHOW_COMMUNITY_POST"

    // Deep Links
    const val DEEP_LINK_CHAOS_ENTRY = "dailychaos://chaos/"
    const val DEEP_LINK_COMMUNITY = "dailychaos://community/"

    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 300L
    const val ANIMATION_DURATION_MEDIUM = 500L
    const val ANIMATION_DURATION_LONG = 1000L

    // Cache Keys
    const val CACHE_KEY_COMMUNITY_FEED = "community_feed"
    const val CACHE_KEY_USER_STATS = "user_stats"
    const val CACHE_KEY_CHAOS_TWINS = "chaos_twins"
}