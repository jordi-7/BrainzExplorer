package com.jordigordillo.brainzexplorer.domain.model

data class Recommendations(
    val featured: List<ArtistSummary>,
    val rock: List<ArtistSummary>,
    val pop: List<ArtistSummary>,
    val electronic: List<ArtistSummary>,
)
