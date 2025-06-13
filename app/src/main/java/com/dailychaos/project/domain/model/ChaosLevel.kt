package com.dailychaos.project.domain.model

/**
 * Chaos Level - Tingkat kekacauan hari ini
 *
 * "Dari 1 (seperti hari biasa Kazuma) sampai 10 (explosion level Megumin)"
 */
enum class ChaosLevel(val value: Int, val description: String, val emoji: String) {
    PEACEFUL(1, "Tenang seperti tidur siang", "😴"),
    CALM(2, "Santai seperti Kazuma malas", "😌"),
    MINOR_CHAOS(3, "Sedikit ribet", "😐"),
    MILD_CHAOS(4, "Mulai agak chaos", "😅"),
    MODERATE_CHAOS(5, "Chaos level standard", "😰"),
    HIGH_CHAOS(6, "Mulai panik", "😨"),
    MAJOR_CHAOS(7, "Chaos beneran", "😱"),
    EXTREME_CHAOS(8, "Level Aqua crying", "😭"),
    DISASTER_CHAOS(9, "Total disaster", "🤯"),
    EXPLOSION_CHAOS(10, "EXPLOSION! Level Megumin", "💥");

    companion object {
        fun fromValue(value: Int): ChaosLevel {
            return values().find { it.value == value } ?: MODERATE_CHAOS
        }

        fun getRandomQuote(level: ChaosLevel): String {
            return when (level) {
                PEACEFUL, CALM -> "Hari yang tenang seperti impian Kazuma!"
                MINOR_CHAOS, MILD_CHAOS -> "Chaos kecil ini masih bisa dihandle!"
                MODERATE_CHAOS, HIGH_CHAOS -> "Chaos level menengah, saatnya kerja sama tim!"
                MAJOR_CHAOS, EXTREME_CHAOS -> "Chaos besar! Butuh skill party yang solid!"
                DISASTER_CHAOS -> "Ini level Aqua menangis, tapi kita masih bisa bangkit!"
                EXPLOSION_CHAOS -> "EXPLOSION! Chaos maksimal tapi spectacular!"
            }
        }
    }
}