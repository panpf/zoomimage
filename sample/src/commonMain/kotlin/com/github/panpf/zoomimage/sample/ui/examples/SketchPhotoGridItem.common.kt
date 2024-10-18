package com.github.panpf.zoomimage.sample.ui.examples

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
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.ability.mimeTypeLogo
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.ImageRequest
import com.github.panpf.sketch.request.ImageResult
import com.github.panpf.sketch.resize.LongImagePrecisionDecider
import com.github.panpf.sketch.resize.LongImageScaleDecider
import com.github.panpf.sketch.state.rememberIconPainterStateImage
import com.github.panpf.zoomimage.sample.image.realSize
import com.github.panpf.zoomimage.sample.resources.Res
import com.github.panpf.zoomimage.sample.resources.ic_image_broken_outline
import com.github.panpf.zoomimage.sample.resources.ic_image_outline
import com.github.panpf.zoomimage.sample.ui.components.InfoItems
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.InfoItem
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.ui.util.rememberMimeTypeLogoMap
import kotlinx.collections.immutable.toImmutableList

@Composable
expect fun ImageRequest.Builder.SketchPhotoGridItemImageConfig()

@Composable
fun SketchPhotoGridItem(
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

    AsyncImage(
        request = request,
        state = imageState,
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

fun buildImageInfos(result: ImageResult): List<InfoItem> = buildList {
    add(InfoItem(title = null, content = result.request.uri.toString()))

    if (result is ImageResult.Success) {
        val optionsInfo = result.cacheKey
            .replace(result.request.uri.toString(), "")
            .let { if (it.startsWith("?")) it.substring(1) else it }
            .split("&")
            .filter { it.trim().isNotEmpty() }
            .joinToString(separator = "\n")
        add(InfoItem(title = "Options: ", content = optionsInfo))

        val sourceImageInfo = result.imageInfo.run {
            "${width}x${height}, $mimeType"
        }
        add(InfoItem(title = "Source Image: ", content = sourceImageInfo))

        add(InfoItem(title = "Result Image: ", content = "${result.image.realSize}"))

        val dataFromInfo = result.dataFrom.name
        add(InfoItem(title = "Data From: ", content = dataFromInfo))

        val transformedInfo = result.transformeds
            ?.joinToString(separator = "\n") { transformed ->
                transformed.replace("Transformed", "")
            }
        add(InfoItem(title = "Transformeds: ", content = transformedInfo.orEmpty()))
    } else if (result is ImageResult.Error) {
        val throwableString = result.throwable.toString()
        add(InfoItem(title = "Throwable: ", content = throwableString))
    }
}