// File: app/src/main/java/com/dailychaos/project/domain/repository/TimeFilterExtensions.kt
package com.dailychaos.project.domain.repository

/**
 * Extension properties dan functions untuk TimeFilter
 */

val TimeFilter.displayName: String
    get() = when (this) {
        TimeFilter.ALL -> "Semua"
        TimeFilter.TODAY -> "Hari Ini"
        TimeFilter.WEEK -> "Minggu Ini"
        TimeFilter.MONTH -> "Bulan Ini"
    }