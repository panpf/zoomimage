package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
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
fun BasicPhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    var photoInfoImageResult by remember { mutableStateOf<ImageResult?>(null) }

    val imageState = rememberAsyncImageState()
    val mimeTypeLogoMap = rememberMimeTypeLogoMap()

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

    Image(
        painter = rememberAsyncImagePainter(
            request = request,
            state = imageState,
            contentScale = ContentScale.Crop,
        ),
        modifier = modifier
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
            .mimeTypeLogo(imageState, mimeTypeLogoMap, margin = 4.dp),
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )

    if (photoInfoImageResult != null) {
        PhotoInfoDialog(photoInfoImageResult) {
            photoInfoImageResult = null
        }
    }
}