package com.github.panpf.zoomimage.sample.ui.examples

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.github.panpf.zoomimage.compose.glide.internal.CrossFade
import com.github.panpf.zoomimage.compose.glide.internal.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.compose.glide.internal.GlideImage
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlidePhotoGridItem(
    index: Int,
    photo: Photo,
    modifier: Modifier,
    onClick: (photo: Photo, index: Int) -> Unit,
) {
    val sketchImageUri = photo.listThumbnailUrl
    val glideModel = remember(sketchImageUri) {
        sketchUri2GlideModel(sketchImageUri)
    }
    GlideImage(
        model = glideModel,
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
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
        transition = CrossFade
    ) {
        it.placeholder(R.drawable.im_placeholder)
            .error(R.drawable.im_error)
    }

    // TODO info dialog
//    if (photoInfoImageResult != null) {
//        PhotoInfoDialog(photoInfoImageResult) {
//            photoInfoImageResult = null
//        }
//    }
}