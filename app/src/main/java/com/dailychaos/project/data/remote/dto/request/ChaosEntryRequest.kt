// File: app/src/main/java/com/dailychaos/project/data/remote/dto/request/ChaosEntryRequest.kt
package com.dailychaos.project.data.remote.dto.request

import kotlinx.serialization.Serializable

/**
 * Chaos Entry Request DTO
 * "Untuk kirim cerita chaos harian - let the adventure begin!"
 */
@Serializable
data class ChaosEntryRequest(
    val title: String,
    val content: String,
    val chaosLevel: Int, // 1-10
    val mood: String, // "happy", "sad", "angry", "excited", "confused", "peaceful"
    val tags: List<String> = emptyList(),
    val isAnonymous: Boolean = false,
    val shareToFeed: Boolean = false,
    val location: LocationData? = null,
    val attachments: List<AttachmentData> = emptyList(),
    val weatherData: WeatherData? = null,
    val miniWins: List<String> = emptyList(),
    val gratitudeNotes: List<String> = emptyList(),
    val tomorrowGoals: List<String> = emptyList()
)

@Serializable
data class LocationData(
    val city: String,
    val country: String,
    val timezone: String,
    val coordinates: CoordinateData? = null
)

@Serializable
data class CoordinateData(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class AttachmentData(
    val type: String, // "image", "audio", "document"
    val url: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)

@Serializable
data class WeatherData(
    val condition: String, // "sunny", "rainy", "cloudy", "stormy"
    val temperature: Double,
    val humidity: Int,
    val description: String
)