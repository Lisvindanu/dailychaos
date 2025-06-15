// File: app/src/main/java/com/dailychaos/project/domain/model/UserProfile.kt
data class UserProfile(
    val userId: String,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val chaosEntries: Int = 0,
    val dayStreak: Int = 0,
    val supportGiven: Int = 0,
    val joinDate: String,
    val authType: String // "username" or "email"
)