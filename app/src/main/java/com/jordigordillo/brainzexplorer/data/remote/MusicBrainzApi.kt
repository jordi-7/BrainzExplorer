package com.jordigordillo.brainzexplorer.data.remote

import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistDto
import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicBrainzApi {

    @GET("artist")
    suspend fun searchArtists(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fmt") format: String = "json",
    ): ArtistSearchResponseDto

    @GET("artist/{mbid}")
    suspend fun lookupArtist(
        @Path("mbid") mbid: String,
        @Query("inc") include: String = "release-groups",
        @Query("fmt") format: String = "json",
    ): ArtistDto
}
