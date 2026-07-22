package com.jordigordillo.brainzexplorer.domain.model

data class ReleaseGroup(
    val id: String,
    val title: String,
    val primaryType: String?,
    val secondaryTypes: List<String> = emptyList(),
    val firstReleaseDate: String?,
)

// Primary types first, then secondary types, in MusicBrainz's own documented order.
private val TYPE_DISPLAY_ORDER = listOf(
    "Album", "Single", "EP", "Broadcast", "Other", "Audio drama", "Audiobook", "Compilation",
    "Demo", "DJ-mix", "Field recording", "Interview", "Live", "Mixtape/Street", "Remix",
    "Soundtrack", "Spokenword"
)

fun ReleaseGroup.allTypes(): Set<String> = setOfNotNull(primaryType) + secondaryTypes

fun List<ReleaseGroup>.availableTypes(): List<String> =
    flatMap { it.allTypes() }
        .distinct()
        .sortedBy { TYPE_DISPLAY_ORDER.indexOf(it).let { i -> if (i < 0) Int.MAX_VALUE else i } }

fun List<ReleaseGroup>.filterByTypes(selected: Set<String>): List<ReleaseGroup> =
    if (selected.isEmpty()) this else filter { it.allTypes().any { type -> type in selected } }

fun List<ReleaseGroup>.sortedByDate(order: ReleaseGroupSortOrder): List<ReleaseGroup> {
    val ascending = sortedBy { it.firstReleaseDate ?: "" } // Date format: YYYY-MM-DD
    return if (order == ReleaseGroupSortOrder.NEWEST_FIRST) ascending.reversed() else ascending
}
