package com.jordigordillo.brainzexplorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.jordigordillo.brainzexplorer.domain.model.ArtistSummary

private val CAROUSEL_IMAGE_SHAPE = RoundedCornerShape(24.dp)

@Composable
fun ArtistCarouselItem(
    artist: ArtistSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CAROUSEL_IMAGE_SHAPE)
            .clickable(onClick = onClick)
    ) {
        if (artist.imageUrl != null) {
            var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = artist.name,
                contentScale = ContentScale.Crop,
                onState = { imageState = it },
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
            when (imageState) {
                AsyncImagePainter.State.Empty, is AsyncImagePainter.State.Loading ->
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        shape = CAROUSEL_IMAGE_SHAPE
                    )
                is AsyncImagePainter.State.Error -> ArtistInitialsAvatar(artist.name)
                is AsyncImagePainter.State.Success -> {}
            }
        } else {
            ArtistInitialsAvatar(artist.name)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        ) {
            Text(
                text = artist.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun ArtistCarouselItemSkeleton(modifier: Modifier = Modifier) {
    SkeletonBox(
        modifier = modifier.aspectRatio(1f),
        shape = CAROUSEL_IMAGE_SHAPE
    )
}

@Composable
fun ArtistInitialsAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtistCarouselItemPreview() {
    ArtistCarouselItem(
        artist = ArtistSummary(
            id = "",
            name = "Architects",
            disambiguation = null,
            imageUrl = null
        ),
        modifier = Modifier.padding(24.dp),
        onClick = {}
    )
}
