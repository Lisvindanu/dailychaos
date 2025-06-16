package com.dailychaos.project.data.remote.api

import com.dailychaos.project.domain.model.CharacterCard
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * KonoSuba API Service
 * Integrates dengan berbagai sumber character data
 */
interface KonoSubaApiService {

    @GET("characters")
    suspend fun getAllCharacters(): List<CharacterCard>

    @GET("characters/{name}")
    suspend fun getCharacterCard(@Path("name") characterName: String): CharacterCard?

    @GET("characters/random")
    suspend fun getRandomCharacterCard(): CharacterCard?

    @GET("characters")
    suspend fun getCharactersByRarity(@Query("rarity") rarity: Int): List<CharacterCard>
}