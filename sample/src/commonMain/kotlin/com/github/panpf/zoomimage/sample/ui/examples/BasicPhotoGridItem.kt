package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.ability.mimeTypeLogo
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.state.rememberIconPainterStateImage
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_image_broken_outline
import com.github.panpf.zoomimage.sample.resources.ic_image_outline
import com.github.panpf.zoomimage.sample.ui.components.InfoItems
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.rememberMimeTypeLogoMap
import kotlinx.collections.immutable.toImmutableList

@Composable
fun BasicPhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val dialogState = rememberMyDialogState()
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
        error(
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
        SketchPhotoGridItemImageConfig()
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
                    onLongPress = { dialogState.show() }
                )
            }
            .mimeTypeLogo(imageState, mimeTypeLogoMap, margin = 4.dp),
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )

    MyDialog(dialogState) {
        val infoItems by remember {
            derivedStateOf {
                imageState.result?.let { buildImageInfos(it) }?.toImmutableList()
            }
        }
        val infoItems1 = infoItems
        if (infoItems1 != null) {
            InfoItems(infoItems1)
        } else {
            Box(Modifier.fillMaxSize().padding(20.dp)) {
                CircularProgressIndicator(Modifier.size(40.dp).align(Alignment.Center))
            }
        }
    }
}