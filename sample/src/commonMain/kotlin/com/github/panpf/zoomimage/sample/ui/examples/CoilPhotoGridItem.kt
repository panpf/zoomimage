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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.request.ErrorResult
import coil3.request.SuccessResult
import com.github.panpf.zoomimage.sample.image.sketchUri2CoilModel
import com.github.panpf.zoomimage.sample.ui.components.InfoItems
import com.github.panpf.zoomimage.sample.ui.components.MyDialog
import com.github.panpf.zoomimage.sample.ui.components.rememberMyDialogState
import com.github.panpf.zoomimage.sample.ui.model.InfoItem
import com.github.panpf.zoomimage.sample.ui.model.Photo
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CoilPhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val context = LocalPlatformContext.current
    val dialogState = rememberMyDialogState()
    var result by remember { mutableStateOf<coil3.request.ImageResult?>(null) }
    val sketchImageUri = photo.listThumbnailUrl
    val coilModel = remember(sketchImageUri) {
        sketchUri2CoilModel(context, sketchImageUri)
    }
    coil3.compose.AsyncImage(
        model = coilModel,
        modifier = modifier
            .pointerInput(photo, index) {
                detectTapGestures(
                    onTap = { onClick(photo, index) },
                    onLongPress = { dialogState.show() }
                )
            },
        placeholder = ColorPainter(MaterialTheme.colorScheme.primaryContainer),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
        onSuccess = {
            result = it.result
        },
        onError = {
            result = it.result
        }
    )

    MyDialog(dialogState) {
        val infoItems by remember {
            derivedStateOf {
                result?.let { buildImageInfos(it) }?.toImmutableList()
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

@OptIn(ExperimentalCoilApi::class)
fun buildImageInfos(result: coil3.request.ImageResult): List<InfoItem> = buildList {
    add(InfoItem(title = null, content = result.request.data.toString()))

    if (result is SuccessResult) {
        add(InfoItem(title = "memoryCacheKey: ", content = result.memoryCacheKey.toString()))
        add(
            InfoItem(
                title = "imageSize: ",
                content = "${result.image.width}x${result.image.height}"
            )
        )
        add(InfoItem(title = "dataSource: ", content = result.dataSource.name))
        add(InfoItem(title = "isSampled: ", content = result.isSampled.toString()))
    } else if (result is ErrorResult) {
        val throwableString = result.throwable.toString()
        add(InfoItem(title = "Throwable: ", content = throwableString))
    }
}