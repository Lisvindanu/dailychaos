package com.dailychaos.project.util

import com.dailychaos.project.domain.model.ChaosLevel
import kotlin.random.Random

/**
 * KonoSuba Quotes dan Motivasi - Full Indonesian Natural Language
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
     * All quotes organized by character and context - Indonesian Natural Language
     */
    private val quotes = listOf(
        // Kazuma - Realistis tapi peduli, bahasa anak muda Indonesia
        Quote("Hidup emang ribet, tapi untung ada temen yang sama ribetnya", Character.KAZUMA),
        Quote("Kadang strategi terbaik itu ya improvisasi aja di detik terakhir", Character.KAZUMA),
        Quote("Gak ada party yang sempurna, yang ada cuma party yang saling nutup kekurangan", Character.KAZUMA),
        Quote("Jadi leader itu cape sih, tapi liat party berkembang worth it banget", Character.KAZUMA),
        Quote("Santai aja, hidup mah gak usah terlalu dipikirin berat", Character.KAZUMA),
        Quote("Yang namanya gagal mah biasa, yang penting besok kita coba lagi", Character.KAZUMA, QuoteContext.FAILURE),
        Quote("Menang dikit juga tetep menang, rayain dulu sebelum chaos berikutnya", Character.KAZUMA, QuoteContext.SUCCESS),
        Quote("Kita emang berantakan, tapi kita keluarga yang berantakan", Character.KAZUMA, QuoteContext.COMMUNITY),
        Quote("Masa sulit mah gak bakal lama, yang lama tuh party yang keras kepala", Character.KAZUMA, QuoteContext.PERSEVERANCE),
        Quote("Chaos tuh cuma hidup yang lagi teriak 'kejutan!' dengan kenceng", Character.KAZUMA, QuoteContext.CHAOS_HIGH),
        Quote("Hari tenang gini malah bikin was-was, pasti ada apa-apa", Character.KAZUMA, QuoteContext.CHAOS_LOW),
        Quote("Party lu bakal support lu, meski kadang mereka yang bikin lu butuh support", Character.KAZUMA, QuoteContext.SUPPORT),
        Quote("Dewasa itu kayak jadi adventurer level rendah di dunia level tinggi", Character.KAZUMA),
        Quote("Senin tuh cuma boss battle dadakan", Character.KAZUMA),
        Quote("Hari baru, kesempatan baru buat gak ngacau... hopefully", Character.KAZUMA),

        // Aqua - Optimis tapi chaos, bahasa cewek Indonesia yang cheerful
        Quote("Tenang aja! Masalah tuh cuma tantangan yang belum ketemu solusinya", Character.AQUA),
        Quote("Setiap hari tuh berkah! Cuma kadang berkahnya agak... explosive gitu", Character.AQUA),
        Quote("Kamu udah amazing kok sebenernya! Dewi kan gak pernah bohong", Character.AQUA),
        Quote("Hari buruk tuh cuma hari baik yang lagi cosplay aja", Character.AQUA),
        Quote("Air mata tuh cuma kebahagiaan yang salah arah", Character.AQUA),
        Quote("Kamu gak sendirian kok, ada aku dan temen-temen yang lain!", Character.AQUA, QuoteContext.SUPPORT),
        Quote("Hari sepi gini perfect buat latihan party tricks!", Character.AQUA, QuoteContext.CHAOS_LOW),
        Quote("Chaos level maksimal? Saatnya tunjukin power dewi!", Character.AQUA, QuoteContext.CHAOS_HIGH),
        Quote("Gagal tuh cuma cerita sukses yang nyasar", Character.AQUA, QuoteContext.FAILURE),
        Quote("Waktunya victory dance! Menang kecil juga harus dirayain gede", Character.AQUA, QuoteContext.SUCCESS),
        Quote("Party terbaik tuh yang bikin semua orang ngerasa diterima", Character.AQUA, QuoteContext.COMMUNITY),
        Quote("Terus semangat! Aku bakal cheering kamu terus", Character.AQUA, QuoteContext.PERSEVERANCE),
        Quote("Berkat dewi kerja paling baik kalau kamu juga usaha", Character.AQUA),
        Quote("Jadi dewi tuh kadang harus kotor-kotoran urusin masalah manusia", Character.AQUA),
        Quote("Pagi tuh cara alam bilang 'siap round dua?'", Character.AQUA),
        Quote("Monday blues tuh cuma debuff sementara", Character.AQUA),

        // Megumin - Passionate dengan filosofi mendalam, bahasa anak muda yang passionate
        Quote("Cari passion kamu terus kejar dengan dedikasi yang explosive!", Character.MEGUMIN),
        Quote("Satu momen perfect itu lebih berharga dari seribu momen biasa-biasa aja", Character.MEGUMIN),
        Quote("Dedikasi bukan soal jadi sempurna, tapi soal komitmen", Character.MEGUMIN),
        Quote("Excellence itu butuh pengorbanan, tapi hasilnya selalu spectacular", Character.MEGUMIN),
        Quote("Mending nguasain satu hal sempurna daripada banyak hal setengah-setengah", Character.MEGUMIN),
        Quote("Seni sejati butuh presisi dan passion", Character.MEGUMIN),
        Quote("EXPLOSION! Kadang cuma itu doang motivasi yang lu butuhin", Character.MEGUMIN, QuoteContext.CHAOS_HIGH),
        Quote("Meski hari tenang, aku tetep planning masterpiece berikutnya", Character.MEGUMIN, QuoteContext.CHAOS_LOW),
        Quote("Explosion yang gagal ngajarin kamu lebih banyak dari yang perfect", Character.MEGUMIN, QuoteContext.FAILURE),
        Quote("Kalau magic kamu berhasil perfect, seluruh dunia pasti tau", Character.MEGUMIN, QuoteContext.SUCCESS),
        Quote("Setiap artist butuh audience, termasuk yang suka ledak-ledakan", Character.MEGUMIN, QuoteContext.COMMUNITY),
        Quote("Terus latihan sampai mimpi kamu jadi kenyataan", Character.MEGUMIN, QuoteContext.PERSEVERANCE),
        Quote("Support mimpi temen itu juga sejenis magic", Character.MEGUMIN, QuoteContext.SUPPORT),
        Quote("Kreativitas tuh bisa muncul di tempat paling aneh", Character.MEGUMIN),
        Quote("Deadline tuh kayak spell cooldown - hargai atau sengsara", Character.MEGUMIN),
        Quote("Setiap sunrise itu kesempatan buat masterpiece baru", Character.MEGUMIN),

        // Darkness - Resilient dengan kehangatan genuine, bahasa yang mature tapi approachable
        Quote("Kekuatan bukan soal gak pernah terluka, tapi soal lindungi orang lain sambil sembuh", Character.DARKNESS),
        Quote("Setiap pukulan yang kamu tanggung buat orang lain bikin kamu makin kuat", Character.DARKNESS),
        Quote("Kemuliaan sejati itu angkat orang lain, terutama pas kamu lagi down", Character.DARKNESS),
        Quote("Keberanian bukan tanpa takut, tapi tetep tegak meski takut", Character.DARKNESS),
        Quote("Defense terbaik itu peduli cukup untuk gak pernah nyerah", Character.DARKNESS),
        Quote("Kehormatan itu pegang janji, terutama yang susah", Character.DARKNESS),
        Quote("Aku bakal tank semua damage biar kamu bisa shine", Character.DARKNESS, QuoteContext.SUPPORT),
        Quote("Bawa aja chaosnya! Makanya aku ada di sini", Character.DARKNESS, QuoteContext.CHAOS_HIGH),
        Quote("Waktu tenang buat healing dan persiapan fight berikutnya", Character.DARKNESS, QuoteContext.CHAOS_LOW),
        Quote("Setiap kegagalan ngajarin gimana cara bangkit lebih kuat lagi", Character.DARKNESS, QuoteContext.FAILURE),
        Quote("Kemenangan paling manis itu yang dibagi sama orang yang kamu lindungi", Character.DARKNESS, QuoteContext.SUCCESS),
        Quote("Party sejati itu tetep bareng through everything", Character.DARKNESS, QuoteContext.COMMUNITY),
        Quote("Daya tahan itu virtue, apalagi kalau buat bantu orang lain", Character.DARKNESS, QuoteContext.PERSEVERANCE),
        Quote("Kadang kekuatan terbesar itu ngaku kalau kamu butuh bantuan", Character.DARKNESS),
        Quote("Lindungi mimpi orang itu panggilan tertinggi", Character.DARKNESS),
        Quote("Self-care itu ultimate defensive spell", Character.DARKNESS),
        Quote("Subuh bawa kekuatan segar buat yang butuh", Character.DARKNESS),

        // Party Wisdom - Insight kolektif tentang friendship dan growth
        Quote("Party paling disfungsional sering punya ikatan paling kuat", Character.PARTY),
        Quote("Orang gak sempurna yang bikin momen sempurna bareng-bareng", Character.PARTY),
        Quote("Chaos jadi indah kalau dihadapi sama temen", Character.PARTY),
        Quote("Kekuatan individual gak ada artinya tanpa trust antar teammates", Character.PARTY),
        Quote("Adventure terbaik itu yang semua orang pulang udah berubah", Character.PARTY),
        Quote("Growth terjadi di celah-celah perbedaan kita", Character.PARTY),
        Quote("Setiap member party bawa sesuatu yang gak bisa digantiin", Character.PARTY, QuoteContext.SUPPORT),
        Quote("Hari chaos tinggi test ikatan kita dan bikin makin kuat", Character.PARTY, QuoteContext.CHAOS_HIGH),
        Quote("Momen damai ngingetin kenapa kita berjuang sekeras ini", Character.PARTY, QuoteContext.CHAOS_LOW),
        Quote("Party gagal jadi cerita yang kita ketawain nanti", Character.PARTY, QuoteContext.FAILURE),
        Quote("Kemenangan bareng infinitely lebih bagus dari solo", Character.PARTY, QuoteContext.SUCCESS),
        Quote("Komunitas sejati itu terima disaster indah satu sama lain", Character.PARTY, QuoteContext.COMMUNITY),
        Quote("Bareng-bareng kita bisa lewatin badai apapun", Character.PARTY, QuoteContext.PERSEVERANCE),
        Quote("Keluarga pilihan sering lebih kuat dari keluarga darah", Character.PARTY),
        Quote("Perjalanan lebih penting dari tujuan kalau punya temen baik", Character.PARTY),
        Quote("Ketawa itu party buff terbaik", Character.PARTY),
        Quote("Prokrastinasi tuh cuma delayed quest completion", Character.PARTY),
        Quote("Setiap hari baru itu adventure segar yang nunggu kejadian", Character.PARTY),

        // Quotes untuk konteks spesifik - lebih relatable untuk Indonesia
        Quote("Yang namanya hidup tuh gak ada manual booknya", Character.KAZUMA),
        Quote("Miracle tuh cuma keajaiban yang kerja overtime", Character.AQUA),
        Quote("Passion project itu yang bikin jiwa tetep hidup", Character.MEGUMIN),
        Quote("Tindakan kecil untuk lindungi orang sama pentingnya dengan yang gede", Character.DARKNESS),
        Quote("Kebahagiaan itu buff terbaik buat party", Character.PARTY),

        // Motivasi sehari-hari yang relate
        Quote("Sarapan itu fuel buat adventure, jangan di-skip", Character.KAZUMA),
        Quote("Cuaca apapun bisa jadi adventure kalau mindset-nya tepat", Character.AQUA),
        Quote("Setiap skill baru itu expansion pack buat kemampuan lu", Character.MEGUMIN),
        Quote("Istirahat yang cukup itu preparation terbaik", Character.DARKNESS),
        Quote("Good vibes itu contagious, sebar sebanyak-banyaknya", Character.PARTY),

        // Quotes goals dan dreams
        Quote("Target kecil yang tercapai lebih baik dari target gede yang mandek", Character.KAZUMA),
        Quote("Mimpi gede butuh action kecil tapi konsisten", Character.AQUA),
        Quote("Master one thing at a time, jangan keburu nafsu", Character.MEGUMIN),
        Quote("Konsistensi itu kunci, bukan perfection", Character.DARKNESS),
        Quote("Progress itu progress, sekecil apapun", Character.PARTY),

        // Seasonal quotes
        Quote("Sore hari tuh perfect buat refleksi sama temen", Character.AQUA),
        Quote("Malam hari bagus buat planning magic besok", Character.MEGUMIN),
        Quote("Istirahat malam itu recovery spell yang wajib", Character.DARKNESS),
        Quote("Weekend itu bonus stage di game kehidupan", Character.PARTY)
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
     * Get daily inspiration quote (weighted towards positive)
     */
    fun getDailyInspiration(): Quote {
        // Weighted random untuk quote yang lebih positif dan inspiratif
        val inspirationalContexts = listOf(
            QuoteContext.GENERAL,
            QuoteContext.GENERAL, // Double weight for general
            QuoteContext.SUCCESS,
            QuoteContext.SUPPORT,
            QuoteContext.COMMUNITY,
            QuoteContext.PERSEVERANCE
        )

        val context = inspirationalContexts.random()
        return getQuoteByContext(context)
    }

    /**
     * Get time-based quote untuk Indonesia
     */
    fun getTimeBasedQuote(): Quote {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> {
                // Pagi - motivasi mulai hari
                quotes.filter {
                    it.text.contains("pagi", ignoreCase = true) ||
                            it.text.contains("hari", ignoreCase = true) ||
                            it.text.contains("sunrise", ignoreCase = true) ||
                            it.text.contains("subuh", ignoreCase = true) ||
                            it.text.contains("sarapan", ignoreCase = true)
                }.randomOrNull() ?: getRandomQuote()
            }
            in 12..17 -> {
                // Siang/sore - perseverance dan motivasi
                getQuoteByContext(QuoteContext.PERSEVERANCE)
            }
            in 18..23 -> {
                // Malam - refleksi dan community
                quotes.filter {
                    it.text.contains("sore", ignoreCase = true) ||
                            it.text.contains("malam", ignoreCase = true) ||
                            it.context == QuoteContext.COMMUNITY
                }.randomOrNull() ?: getQuoteByContext(QuoteContext.COMMUNITY)
            }
            else -> {
                // Tengah malam/dini hari - supportive
                getQuoteByContext(QuoteContext.SUPPORT)
            }
        }
    }

    /**
     * Get character response untuk chaos level dalam bahasa Indonesia
     */
    fun getCharacterResponse(character: Character, chaosLevel: ChaosLevel): String {
        return when (character) {
            Character.KAZUMA -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Tenang gini malah bikin curiga... pasti ada yang gak beres"
                ChaosLevel.EXPLOSION_CHAOS -> "Mulai deh chaos ini, kayaknya emang nasib party kita"
                else -> "Ya udah biasa lah, hari normal sama party ini"
            }
            Character.AQUA -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Boring banget! Kapan ada excitement lagi?"
                ChaosLevel.EXPLOSION_CHAOS -> "This is fine! Semuanya totally under control!"
                else -> "Berkat dewi bikin semuanya berjalan sempurna!"
            }
            Character.MEGUMIN -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Kondisi perfect buat planning explosive masterpiece berikutnya"
                ChaosLevel.EXPLOSION_CHAOS -> "EXPLOSION! Eh wait, bukan aku yang bikin kali ini..."
                else -> "Setiap hari tuh kesempatan buat magic spectacular!"
            }
            Character.DARKNESS -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Waktu damai bagus buat recovery dan training"
                ChaosLevel.EXPLOSION_CHAOS -> "Ayo aja! Aku bisa handle chaos apapun yang datang"
                else -> "Stand strong buat party, as always"
            }
            Character.PARTY -> when (chaosLevel) {
                ChaosLevel.PEACEFUL -> "Momen tenang gini ngingetin kenapa kita stick together"
                ChaosLevel.EXPLOSION_CHAOS -> "Chaos tuh cuma kata lain buat adventure!"
                else -> "Apapun yang terjadi, kita hadapi bareng-bareng"
            }
        }
    }

    /**
     * Get multiple quotes for variety
     */
    fun getMultipleQuotes(count: Int = 3): List<Quote> {
        return quotes.shuffled().take(count)
    }
}