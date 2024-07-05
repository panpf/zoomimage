package com.github.panpf.zoomimage.sample.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.SubcomposeAsyncImage
import com.github.panpf.sketch.ability.mimeTypeLogo
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.request.composableError
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.state.rememberIconPainterStateImage
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_image_broken_outline
import com.github.panpf.zoomimage.sample.resources.ic_image_outline
import com.github.panpf.zoomimage.sample.ui.common.PhotoInfoDialog
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.rememberMimeTypeLogoMap

@Composable
fun PhotoGridItem(
    index: Int,
    photo: Photo,
    staggeredGridMode: Boolean = false,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    var photoInfoImageResult by remember { mutableStateOf<ImageResult?>(null) }

    val imageState = rememberAsyncImageState()
    val mimeTypeLogoMap = rememberMimeTypeLogoMap()
    val modifier = Modifier
        .fillMaxWidth()
        .let {
            val photoWidth = photo.width ?: 0
            val photoHeight = photo.height ?: 0
            if (staggeredGridMode && photoWidth > 0 && photoHeight > 0) {
                it.aspectRatio(photoWidth.toFloat() / photoHeight)
            } else {
                it.aspectRatio(1f)
            }
        }
        .pointerInput(photo, index) {
            detectTapGestures(
                onTap = { onClick(photo, index) },
                onLongPress = {
                    val imageResult = imageState.result
                    if (imageResult != null) {
                        photoInfoImageResult = imageResult
                    }
                }
            )
        }
        .mimeTypeLogo(imageState, mimeTypeLogoMap, margin = 4.dp)

    val colorScheme = MaterialTheme.colorScheme
    val request = ComposableImageRequest(photo.listThumbnailUrl) {
        placeholder(
            rememberIconPainterStateImage(
                icon = Res.drawable.ic_image_outline,
                background = colorScheme.primaryContainer,
                iconTint = colorScheme.onPrimaryContainer
            )
        )
        composableError(
            rememberIconPainterStateImage(
                icon = Res.drawable.ic_image_broken_outline,
                background = colorScheme.primaryContainer,
                iconTint = colorScheme.onPrimaryContainer
            )
        )
        precision(LongImagePrecisionDecider())
        scale(LongImageScaleDecider())
        crossfade()
        resizeOnDraw()
        sizeMultiplier(2f)  // To get a clearer thumbnail
    }
    when (index % 3) {
        0 -> {
            AsyncImage(
                request = request,
                state = imageState,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo",
            )
        }

        1 -> {
            SubcomposeAsyncImage(
                request = request,
                state = imageState,
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo",
            )
        }

        else -> {
            Image(
                painter = rememberAsyncImagePainter(
                    request = request,
                    state = imageState,
                    contentScale = ContentScale.Crop
                ),
                modifier = modifier,
                contentScale = ContentScale.Crop,
                contentDescription = "photo"
            )
        }
    }

    if (photoInfoImageResult != null) {
        PhotoInfoDialog(photoInfoImageResult) {
            photoInfoImageResult = null
        }
    }
}