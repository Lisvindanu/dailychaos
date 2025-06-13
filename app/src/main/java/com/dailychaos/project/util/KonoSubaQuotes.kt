package com.dailychaos.project.util

import com.dailychaos.project.domain.model.ChaosLevel
import kotlin.random.Random

/**
 * KonoSuba Quotes dan Motivasi
 *
 * "Seperti party Kazuma yang selalu punya wisdom di tengah chaos"
 */
object KonoSubaQuotes {

    /**
     * Character-based quotes
     */
    enum class Character(val displayName: String) {
        KAZUMA("Kazuma"),
        AQUA("Aqua"),
        MEGUMIN("Megumin"),
        DARKNESS("Darkness"),
        PARTY("Party")
    }

    /**
     * Quote data class
     */
    data class Quote(
        val text: String,
        val character: Character,
        val context: QuoteContext = QuoteContext.GENERAL
    )

    /**
     * Quote contexts
     */
    enum class QuoteContext {
        GENERAL,           // General motivation
        CHAOS_HIGH,        // For high chaos levels
        CHAOS_LOW,         // For peaceful days
        SUPPORT,           // For giving/receiving support
        FAILURE,           // For handling failures
        SUCCESS,           // For celebrating wins
        COMMUNITY,         // For community interactions
        PERSEVERANCE       // For tough times
    }

    /**
     * All quotes organized by character and context
     */
    private val quotes = listOf(
        // Kazuma - Realistic but caring
        Quote("Bahkan di dunia fantasy pun hidup itu susah, tapi setidaknya kita tidak sendirian", Character.KAZUMA),
        Quote("Hari ini chaos? Besok kita coba strategi baru", Character.KAZUMA),
        Quote("Kadang jadi leader party disfungsional itu susah, tapi mereka adalah keluargaku", Character.KAZUMA),
        Quote("Tidak ada yang sempurna, bahkan hero sekalipun. Yang penting kita terus maju", Character.KAZUMA),
        Quote("Quest gagal? Biasa aja. Yang penting kita belajar dan party masih lengkap", Character.KAZUMA, QuoteContext.FAILURE),
        Quote("Kemenangan kecil juga tetap kemenangan. Celebrate dulu, besok quest lagi", Character.KAZUMA, QuoteContext.SUCCESS),
        Quote("Teamwork itu kunci, meski teamnya agak... unik", Character.KAZUMA, QuoteContext.COMMUNITY),

        // Aqua - Optimistic but chaotic
        Quote("Jangan khawatir! Dewi keberuntungan akan membantu!", Character.AQUA),
        Quote("Semuanya pasti beres! Aku yakin kok!", Character.AQUA),
        Quote("Kamu tidak sendirian, ada aku dan teman-teman lainnya!", Character.AQUA, QuoteContext.SUPPORT),
        Quote("Hari buruk? Impossible! Setiap hari itu blessing!", Character.AQUA, QuoteContext.CHAOS_LOW),
        Quote("Chaos itu normal! Bahkan dewi pun kadang bikin onar", Character.AQUA, QuoteContext.CHAOS_HIGH),
        Quote("Gagal? Itu cuma persiapan untuk sukses yang lebih besar!", Character.AQUA, QuoteContext.FAILURE),
        Quote("Party healing! Semua luka akan sembuh!", Character.AQUA, QuoteContext.SUPPORT),

        // Megumin - Passionate about her thing
        Quote("Passion itu lebih penting dari praktikalitas!", Character.MEGUMIN),
        Quote("Setiap hari adalah kesempatan untuk EXPLOSION baru!", Character.MEGUMIN),
        Quote("Satu ledakan yang sempurna lebih baik dari seribu serangan biasa-biasa saja", Character.MEGUMIN),
        Quote("Dedikasi pada satu hal itu indah, meski orang lain tidak mengerti", Character.MEGUMIN),
        Quote("Chaos level maksimal? EXPLOSION TIME!", Character.MEGUMIN, QuoteContext.CHAOS_HIGH),
        Quote("Bahkan hari tenang pun bisa jadi spectacular dengan perspektif yang tepat", Character.MEGUMIN, QuoteContext.CHAOS_LOW),
        Quote("Kegagalan itu cuma ledakan yang belum sempurna", Character.MEGUMIN, QuoteContext.FAILURE),
        Quote("Menemukan passion-mu itu seperti menemukan spell terkuat", Character.MEGUMIN, QuoteContext.SUCCESS),

        // Darkness - Resilient and protective
        Quote("Kita bisa melewati apapun bersama-sama!", Character.DARKNESS),
        Quote("Bahkan pukulan terberat sekalipun akan berlalu", Character.DARKNESS),
        Quote("Melindungi teman-teman adalah kehormatan terbesar", Character.DARKNESS),
        Quote("Ketangguhan itu tidak datang dari tidak pernah jatuh, tapi dari selalu bangkit", Character.DARKNESS),
        Quote("Tank damage untuk party? With pleasure!", Character.DARKNESS, QuoteContext.SUPPORT),
        Quote("Chaos tinggi? Biar aku yang handle!", Character.DARKNESS, QuoteContext.CHAOS_HIGH),
        Quote("Hari tenang itu bagus untuk recovery dan planning", Character.DARKNESS, QuoteContext.CHAOS_LOW),
        Quote("Gagal itu training untuk jadi lebih kuat", Character.DARKNESS, QuoteContext.FAILURE),
        Quote("Protecting others is the greatest victory", Character.DARKNESS, QuoteContext.SUCCESS),
        Quote("Stand together, fight together, win together", Character.DARKNESS, QuoteContext.COMMUNITY),

        // Party Wisdom
        Quote("Party yang paling disfungsional pun masih bisa selamatkan hari", Character.PARTY),
        Quote("Kekuatan sejati datang dari kelemahan yang saling melengkapi", Character.PARTY),
        Quote("Tidak ada yang sempurna sendiri, tapi bersama kita bisa sempurna", Character.PARTY),
        Quote("Chaos itu indah kalau dihadapi bareng-bareng", Character.PARTY),
        Quote("Setiap anggota party punya value, meski kadang tidak terlihat", Character.PARTY, QuoteContext.SUPPORT),
        Quote("Adventure terbaik datang dari party yang saling peduli", Character.PARTY, QuoteContext.COMMUNITY),
        Quote("Bahkan quest yang gagal jadi memory berharga kalau bareng teman", Character.PARTY, QuoteContext.FAILURE)
    )

    /**
     * Get random quote
     */
    fun getRandomQuote(): Quote {
        return quotes.random()
    }

    /**
     * Get quote by character
     */
    fun getQuoteByCharacter(character: Character): Quote {
        val characterQuotes = quotes.filter { it.character == character }
        return characterQuotes.randomOrNull() ?: getRandomQuote()
    }

    /**
     * Get quote by context
     */
    fun getQuoteByContext(context: QuoteContext): Quote {
        val contextQuotes = quotes.filter { it.context == context }
        return contextQuotes.randomOrNull() ?: getRandomQuote()
    }

    /**
     * Get quote based on chaos level
     */
    fun getQuoteForChaosLevel(chaosLevel: ChaosLevel): Quote {
        return when (chaosLevel) {
            ChaosLevel.PEACEFUL, ChaosLevel.CALM -> {
                getQuoteByContext(QuoteContext.CHAOS_LOW)
            }
            ChaosLevel.DISASTER_CHAOS, ChaosLevel.EXPLOSION_CHAOS -> {
                getQuoteByContext(QuoteContext.CHAOS_HIGH)
            }
            else -> getRandomQuote()
        }
    }

    /**
     * Get motivational quote for specific situation
     */
    fun getMotivationalQuote(
        isSuccess: Boolean = false,
        isFailure: Boolean = false,
        needsSupport: Boolean = false
    ): Quote {
        return when {
            isSuccess -> getQuoteByContext(QuoteContext.SUCCESS)
            isFailure -> getQuoteByContext(QuoteContext.FAILURE)
            needsSupport -> getQuoteByContext(QuoteContext.SUPPORT)
            else -> getRandomQuote()
        }
    }

    /**
     * Get community-focused quote
     */
    fun getCommunityQuote(): Quote {
        return getQuoteByContext(QuoteContext.COMMUNITY)
    }

    /**
     * Get perseverance quote for tough times
     */
    fun getPerseveranceQuote(): Quote {
        return getQuoteByContext(QuoteContext.PERSEVERANCE)
    }

    /**
     * Get daily inspiration quote
     */
    fun getDailyInspiration(): Quote {
        // Weighted random untuk quote yang lebih positif
        val inspirationalContexts = listOf(
            QuoteContext.GENERAL,
            QuoteContext.SUCCESS,
            QuoteContext.SUPPORT,
            QuoteContext.COMMUNITY
        )

        val context = inspirationalContexts.random()
        return getQuoteByContext(context)
    }

    /**
     * Get character-appropriate response for chaos level
     */
    fun getCharacterResponse(character: Character, chaosLevel: ChaosLevel): String {
        return when (character) {
            Character.KAZUMA -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Hari yang tenang... suspicious tapi aku terima aja deh"
                ChaosLevel.EXPLOSION_CHAOS -> "Ini pasti ulah Megumin lagi..."
                else -> "Chaos level standard untuk party kita"
            }
            Character.AQUA -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Boring! Kapan ada quest seru lagi?"
                ChaosLevel.EXPLOSION_CHAOS -> "KYAA! Aku tidak siap untuk ini!"
                else -> "Tenang, dewi keberuntungan akan mengurus semuanya!"
            }
            Character.MEGUMIN -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Hari yang tenang... perfect untuk planning explosion berikutnya"
                ChaosLevel.EXPLOSION_CHAOS -> "EXPLOSION! This is my moment!"
                else -> "Lumayan, tapi masih bisa lebih spectacular"
            }
            Character.DARKNESS -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Hari tenang itu bagus untuk recovery"
                ChaosLevel.EXPLOSION_CHAOS -> "Bring it on! Aku bisa handle ini!"
                else -> "Whatever happens, kita hadapi bersama!"
            }
            Character.PARTY -> "Apapun chaos level-nya, party ini siap!"
        }
    }
}