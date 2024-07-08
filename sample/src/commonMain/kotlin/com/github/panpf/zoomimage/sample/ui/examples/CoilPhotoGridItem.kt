package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

@Composable
fun CoilPhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val context = LocalPlatformContext.current
    val sketchImageUri = photo.listThumbnailUrl
    val coilModel = remember(sketchImageUri) {
        sketchUri2CoilModel(context, sketchImageUri)
    }
    coil3.compose.AsyncImage(
        model = ImageRequest.Builder(context).apply {
            data(coilModel)
            crossfade(true)
        }.build(),
        modifier = modifier
            .pointerInput(photo, index) {
                detectTapGestures(
                    onTap = { onClick(photo, index) },
//                    onLongPress = {
//                        val imageResult = imageState.result
//                        if (imageResult != null) {
//                            photoInfoImageResult = imageResult
//                        }
//                    }
                )
            },
        placeholder = ColorPainter(MaterialTheme.colorScheme.primaryContainer),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )

    // TODO info dialog
//    if (photoInfoImageResult != null) {
//        PhotoInfoDialog(photoInfoImageResult) {
//            photoInfoImageResult = null
//        }
//    }
}