package com.github.panpf.zoomimage.sample.ui.photoalbum.compose

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.github.panpf.sketch.drawable.IconDrawable
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.util.sketchUri2CoilModel

@Composable
fun CoilListImage(sketchImageUri: String, modifier: Modifier) {
    val context = LocalContext.current
    val coilModel = remember(sketchImageUri) {
        sketchUri2CoilModel(context, sketchImageUri)
    }
    AsyncImage(
        model = ImageRequest.Builder(context).apply {
            data(coilModel)
            placeholder(iconDrawable(context, R.drawable.ic_image_outline, R.color.placeholder_bg))
            error(iconDrawable(context, R.drawable.ic_error_baseline, R.color.placeholder_bg))
            crossfade(true)
        }.build(),
        modifier = modifier,
        contentScale = ContentScale.Crop,
        contentDescription = "photo",
    )
}

fun iconDrawable(context: Context, @DrawableRes icon: Int, @ColorRes bg: Int): Drawable {
    return IconDrawable(
        icon = ResourcesCompat.getDrawable(context.resources, icon, null)!!,
        background = ColorDrawable(ResourcesCompat.getColor(context.resources, bg, null)),
    )
}