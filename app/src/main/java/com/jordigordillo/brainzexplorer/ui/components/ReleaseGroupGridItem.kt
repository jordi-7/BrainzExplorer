package com.jordigordillo.brainzexplorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.jordigordillo.brainzexplorer.data.mapper.coverArtUrl
import com.jordigordillo.brainzexplorer.domain.model.ReleaseGroup

private val RELEASE_GROUP_IMAGE_SHAPE = RoundedCornerShape(24.dp)

@Composable
fun ReleaseGroupGridItem(
    releaseGroup: ReleaseGroup,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        shape = RELEASE_GROUP_IMAGE_SHAPE
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RELEASE_GROUP_IMAGE_SHAPE)
        ) {
            var imageState by remember(releaseGroup.id) {
                mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
            }
            AsyncImage(
                model = coverArtUrl(releaseGroup.id),
                contentDescription = releaseGroup.title,
                contentScale = ContentScale.Crop,
                onState = { imageState = it },
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            when (imageState) {
                AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading ->
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = RELEASE_GROUP_IMAGE_SHAPE
                    )
                is AsyncImagePainter.State.Error -> ArtistInitialsAvatar(releaseGroup.title)
                is AsyncImagePainter.State.Success -> {}
            }
        }

        Text(
            text = releaseGroup.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 16.dp)
        )

        val year = releaseGroup.firstReleaseDate?.take(4)
        if (!year.isNullOrBlank()) {
            Text(
                text = year,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp).padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReleaseGroupGridItemPreview() {
    ReleaseGroupGridItem(
        modifier = Modifier.padding(24.dp),
        releaseGroup = ReleaseGroup(
            id = "1",
            title = "Everything Was Sound",
            primaryType = "Album",
            firstReleaseDate = "2016"
        )
    )
}