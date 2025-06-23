// File: app/src/main/java/com/dailychaos/project/domain/model/SupportTypeExtensions.kt
package com.dailychaos.project.domain.model

/**
 * Extensions for SupportType to support comment system
 */

/**
 * Get emoji for support type
 */
val SupportType.emoji: String
    get() = when (this) {
        SupportType.HEART -> "💝"
        SupportType.HUG -> "🤗"
        SupportType.STRENGTH -> "💪"
        SupportType.HOPE -> "🌟"
    }

/**
 * Get display text for support type
 */
val SupportType.displayText: String
    get() = when (this) {
        SupportType.HEART -> "Heart"
        SupportType.HUG -> "Hug"
        SupportType.STRENGTH -> "Strength"
        SupportType.HOPE -> "Hope"
    }

/**
 * Get description for support type in comment context
 */
val SupportType.commentDescription: String
    get() = when (this) {
        SupportType.HEART -> "Sending love and care"
        SupportType.HUG -> "Virtual warm embrace"
        SupportType.STRENGTH -> "You've got this, warrior!"
        SupportType.HOPE -> "Better days are coming"
    }