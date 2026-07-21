package com.jordigordillo.brainzexplorer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary

private val RESULT_ROW_IMAGE_SHAPE = RoundedCornerShape(16.dp)
private val RESULT_ROW_IMAGE_SIZE = 48.dp

@Composable
fun ArtistResultRow(artist: ArtistSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(RESULT_ROW_IMAGE_SIZE)
                .clip(RESULT_ROW_IMAGE_SHAPE)
        ) {
            if (artist.imageUrl != null) {
                var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    onState = { imageState = it },
                    modifier = Modifier.size(RESULT_ROW_IMAGE_SIZE)
                )
                when (imageState) {
                    AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading ->
                        SkeletonBox(
                            modifier = Modifier.size(RESULT_ROW_IMAGE_SIZE),
                            shape = RESULT_ROW_IMAGE_SHAPE
                        )
                    is AsyncImagePainter.State.Error -> ArtistInitialsAvatar(artist.name)
                    is AsyncImagePainter.State.Success -> {}
                }
            } else {
                ArtistInitialsAvatar(artist.name)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = artist.name, style = MaterialTheme.typography.bodyLarge)
            if (!artist.disambiguation.isNullOrBlank()) {
                Text(
                    text = artist.disambiguation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ArtistResultRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        SkeletonBox(
            modifier = Modifier.size(RESULT_ROW_IMAGE_SIZE),
            shape = RESULT_ROW_IMAGE_SHAPE
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            SkeletonBox(modifier = Modifier.width(180.dp).height(18.dp))
            SkeletonBox(modifier = Modifier.padding(top = 6.dp).width(120.dp).height(14.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistResultRowPreview() {
    ArtistResultRow(
        artist = ArtistSummary(
            id = "",
            name = "Architects",
            disambiguation = "British metal band",
            imageUrl = null
        ),
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    )
}
