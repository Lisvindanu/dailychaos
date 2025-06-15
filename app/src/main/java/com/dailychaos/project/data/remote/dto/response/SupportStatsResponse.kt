// File: app/src/main/java/com/dailychaos/project/data/remote/dto/response/SupportStatsResponse.kt
package com.dailychaos.project.data.remote.dto.response

import kotlinx.serialization.Serializable

/**
 * Support Stats Response DTO
 * "Stats tentang dukungan yang diberikan/diterima - measure the love!"
 */
@Serializable
data class SupportStatsResponse(
    val userId: String,
    val supportGiven: SupportMetricsResponse,
    val supportReceived: SupportMetricsResponse,
    val streakInfo: SupportStreakResponse,
    val achievements: List<SupportAchievementResponse>,
    val rankings: SupportRankingResponse,
    val periodStats: Map<String, SupportPeriodResponse>, // "daily", "weekly", "monthly"
    val lastUpdated: Long
)

@Serializable
data class SupportMetricsResponse(
    val total: Int,
    val byType: Map<String, Int>, // "heart" -> 50, "hug" -> 30, etc.
    val byPeriod: Map<String, Int>, // "today" -> 5, "this_week" -> 25, etc.
    val averagePerDay: Double,
    val topRecipients: List<TopSupportUserResponse> = emptyList(),
    val recentActivity: List<RecentSupportActivityResponse> = emptyList()
)

@Serializable
data class TopSupportUserResponse(
    val userId: String,
    val username: String,
    val displayName: String,
    val supportCount: Int,
    val lastSupportAt: Long
)

@Serializable
data class RecentSupportActivityResponse(
    val activityId: String,
    val type: String, // "given", "received"
    val reactionType: String,
    val targetUserId: String,
    val targetUsername: String,
    val contentId: String,
    val contentType: String,
    val timestamp: Long,
    val message: String? = null
)

@Serializable
data class SupportStreakResponse(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakType: String, // "giving", "receiving", "mutual"
    val lastActivityDate: String,
    val streakStartDate: String,
    val daysUntilNextMilestone: Int,
    val nextMilestone: Int
)

@Serializable
data class SupportAchievementResponse(
    val achievementId: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String, // "supporter", "beloved", "consistent", "milestone"
    val unlockedAt: Long? = null,
    val progress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean,
    val reward: AchievementRewardResponse? = null
)

@Serializable
data class AchievementRewardResponse(
    val type: String, // "badge", "title", "theme", "feature"
    val name: String,
    val description: String,
    val icon: String? = null
)

@Serializable
data class SupportRankingResponse(
    val giverRank: Int,
    val receiverRank: Int,
    val overallRank: Int,
    val percentile: Double,
    val categoryRankings: Map<String, Int>, // "daily_giver", "weekly_supporter", etc.
    val leaderboardPosition: LeaderboardPositionResponse
)

@Serializable
data class LeaderboardPositionResponse(
    val position: Int,
    val totalUsers: Int,
    val scoreType: String,
    val score: Int,
    val previousPosition: Int? = null,
    val trend: String // "up", "down", "stable", "new"
)

@Serializable
data class SupportPeriodResponse(
    val period: String,
    val startDate: String,
    val endDate: String,
    val supportGiven: Int,
    val supportReceived: Int,
    val uniqueUsersSupported: Int,
    val uniqueSupporters: Int,
    val averageSupport: Double,
    val bestDay: String? = null,
    val milestones: List<String> = emptyList()
)