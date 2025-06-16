package com.dailychaos.project.data.remote.api

import com.dailychaos.project.domain.model.CharacterCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Corrected KonoSuba API Implementation
 * Menggunakan URL structure yang benar: IconMember/Source/
 */
@Singleton
class KonoSubaApiImpl @Inject constructor() : KonoSubaApiService {

    companion object {
        // CORRECTED Base URL berdasarkan contoh yang working
        private const val GITHUB_RAW_BASE = "https://raw.githubusercontent.com/HaiKonofanDesu/konofan-assets-jp-sortet/main/"
        private const val ICON_MEMBER_BASE = "${GITHUB_RAW_BASE}Assets/AddressableAssetsStore/UnityAssets/Common/Images/IconMember/Source/"
    }

    // Character ID mapping berdasarkan repo structure
    private val characterIdMap = mapOf(
        "Kazuma" to "100",
        "Aqua" to "101",
        "Megumin" to "102",
        "Darkness" to "103",
        "Wiz" to "104",
        "Yunyun" to "105",
        "Chris" to "106"
    )

    // Verified working image IDs berdasarkan pattern 1001100
    private val workingImageIds = mapOf(
        "Kazuma" to listOf("1001100", "1002100", "1003100", "1004100"),
        "Aqua" to listOf("1011100", "1012100", "1013100", "1014100"),
        "Megumin" to listOf("1021100", "1022100", "1023100", "1024100"),
        "Darkness" to listOf("1031100", "1032100", "1033100", "1034100")
    )

    private val characterData = mapOf(
        "Kazuma" to Triple(3, "Adventure", "The reluctant hero leading this chaotic party"),
        "Aqua" to Triple(4, "Water", "Goddess of water with questionable wisdom"),
        "Megumin" to Triple(4, "Explosion", "Crimson Magic Clan's explosive arch wizard"),
        "Darkness" to Triple(4, "Defense", "Masochistic crusader with impeccable defense")
    )

    override suspend fun getAllCharacters(): List<CharacterCard> = withContext(Dispatchers.IO) {
        return@withContext workingImageIds.keys.map { name ->
            createCharacterCard(name)
        }
    }

    override suspend fun getCharacterCard(characterName: String): CharacterCard? = withContext(Dispatchers.IO) {
        val imageIds = workingImageIds[characterName]
        return@withContext if (imageIds != null) {
            val randomImageId = imageIds.random()
            createCharacterCard(characterName, randomImageId)
        } else {
            null
        }
    }

    override suspend fun getRandomCharacterCard(): CharacterCard? = withContext(Dispatchers.IO) {
        val randomCharacter = workingImageIds.keys.random()
        return@withContext getCharacterCard(randomCharacter)
    }

    override suspend fun getCharactersByRarity(rarity: Int): List<CharacterCard> = withContext(Dispatchers.IO) {
        return@withContext getAllCharacters().filter { it.rarity == rarity }
    }

    private fun createCharacterCard(name: String, imageId: String? = null): CharacterCard {
        val (rarity, element, description) = characterData[name] ?: Triple(3, "Unknown", "A party member")

        // Use specific image ID or random from available ones
        val finalImageId = imageId ?: workingImageIds[name]?.random() ?: "1001100"
        val imageUrl = "$ICON_MEMBER_BASE$finalImageId.png"

        return CharacterCard(
            id = "${name.lowercase()}_$finalImageId",
            name = name,
            imageUrl = imageUrl,
            rarity = extractRarityFromImageId(finalImageId),
            element = element,
            description = description
        )
    }

    /**
     * Extract rarity dari image ID
     * Format: 1001100 -> Character 100, Rarity 1, Event 100
     */
    private fun extractRarityFromImageId(imageId: String): Int {
        return if (imageId.length >= 4) {
            imageId[3].toString().toIntOrNull() ?: 3
        } else {
            3
        }
    }
}