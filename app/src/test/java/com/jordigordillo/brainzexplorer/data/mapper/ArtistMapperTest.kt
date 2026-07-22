package com.jordigordillo.brainzexplorer.data.mapper

import com.jordigordillo.brainzexplorer.data.remote.dto.ArtistDto
import com.jordigordillo.brainzexplorer.data.remote.dto.ReleaseGroupDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtistMapperTest {

    @Test
    fun `filterKnownArtists excludes Various Artists and unknown but keeps others in order`() {
        val artists = listOf(
            ArtistDto(id = "abc", name = "Radiohead"),
            ArtistDto(id = "89ad4ac3-39f7-470e-963a-56509c546377", name = "Various Artists"),
            ArtistDto(id = "125ec42a-7229-4250-afc5-e057484327fe", name = "[unknown]"),
            ArtistDto(id = "def", name = "Muse")
        )

        val result = artists.filterKnownArtists()

        assertEquals(listOf("abc", "def"), result.map { it.id })
    }

    @Test
    fun `coverArtUrl builds the coverartarchive front-250 url`() {
        assertEquals(
            "https://coverartarchive.org/release-group/rg1/front-250",
            coverArtUrl("rg1")
        )
    }

    @Test
    fun `representativeImageUrl returns null when there are no release groups`() {
        val artist = ArtistDto(id = "abc", name = "Radiohead")

        assertNull(artist.representativeImageUrl())
    }

    @Test
    fun `representativeImageUrl returns null when no release group is an Album`() {
        val artist = ArtistDto(
            id = "abc",
            name = "Radiohead",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "Live at Reading", primaryType = "EP")
            )
        )

        assertNull(artist.representativeImageUrl())
    }

    @Test
    fun `representativeImageUrl picks the first Album release group`() {
        val artist = ArtistDto(
            id = "abc",
            name = "Radiohead",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "Live at Reading", primaryType = "EP"),
                ReleaseGroupDto(id = "rg2", title = "OK Computer", primaryType = "Album"),
                ReleaseGroupDto(id = "rg3", title = "Kid A", primaryType = "Album")
            )
        )

        assertEquals(
            "https://coverartarchive.org/release-group/rg2/front-250",
            artist.representativeImageUrl()
        )
    }

    @Test
    fun `toSummary defaults imageUrl to representativeImageUrl`() {
        val artist = ArtistDto(
            id = "abc",
            name = "Radiohead",
            disambiguation = "UK rock band",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "OK Computer", primaryType = "Album")
            )
        )

        val summary = artist.toSummary()

        assertEquals("abc", summary.id)
        assertEquals("Radiohead", summary.name)
        assertEquals("UK rock band", summary.disambiguation)
        assertEquals("https://coverartarchive.org/release-group/rg1/front-250", summary.imageUrl)
    }

    @Test
    fun `toSummary honors an explicit imageUrl override`() {
        val artist = ArtistDto(
            id = "abc",
            name = "Radiohead",
            releaseGroups = listOf(
                ReleaseGroupDto(id = "rg1", title = "OK Computer", primaryType = "Album")
            )
        )

        val summary = artist.toSummary(imageUrl = null)

        assertNull(summary.imageUrl)
    }

    @Test
    fun `toDetail maps fields and release groups`() {
        val artist = ArtistDto(
            id = "abc",
            name = "Radiohead",
            disambiguation = "UK rock band",
            country = "GB",
            releaseGroups = listOf(
                ReleaseGroupDto(
                    id = "rg1",
                    title = "OK Computer",
                    primaryType = "Album",
                    secondaryTypes = listOf("Live"),
                    firstReleaseDate = "1997-05-21"
                )
            )
        )

        val detail = artist.toDetail()

        assertEquals("abc", detail.id)
        assertEquals("Radiohead", detail.name)
        assertEquals("UK rock band", detail.disambiguation)
        assertEquals("GB", detail.country)
        assertEquals(1, detail.releaseGroups.size)
        assertEquals("OK Computer", detail.releaseGroups[0].title)
        assertEquals(listOf("Live"), detail.releaseGroups[0].secondaryTypes)
        assertEquals("1997-05-21", detail.releaseGroups[0].firstReleaseDate)
    }

    @Test
    fun `toDetail maps to an empty release group list when null`() {
        val artist = ArtistDto(id = "abc", name = "Radiohead")

        val detail = artist.toDetail()

        assertEquals(emptyList<Any>(), detail.releaseGroups)
    }

    @Test
    fun `ReleaseGroupDto toDomain maps all fields`() {
        val dto = ReleaseGroupDto(
            id = "rg1",
            title = "OK Computer",
            primaryType = "Album",
            secondaryTypes = listOf("Live", "Remix"),
            firstReleaseDate = "1997-05-21"
        )

        val domain = dto.toDomain()

        assertEquals("rg1", domain.id)
        assertEquals("OK Computer", domain.title)
        assertEquals("Album", domain.primaryType)
        assertEquals(listOf("Live", "Remix"), domain.secondaryTypes)
        assertEquals("1997-05-21", domain.firstReleaseDate)
    }
}
