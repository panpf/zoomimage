package com.github.panpf.zoomimage.sample.ui.examples

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.panpf.sketch.fetch.newResourceUri
import com.github.panpf.zoomimage.GlideZoomAsyncImage
import com.github.panpf.zoomimage.compose.glide.internal.ExperimentalGlideComposeApi
import com.github.panpf.zoomimage.sample.R
import com.github.panpf.zoomimage.sample.image.PhotoPalette
import com.github.panpf.zoomimage.sample.ui.components.MyPageState
import com.github.panpf.zoomimage.sample.ui.components.PageState
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideZoomAsyncImageSample(
    sketchImageUri: String,
    photoPaletteState: MutableState<PhotoPalette>
) {
    BaseZoomImageSample(
        sketchImageUri = sketchImageUri,
        photoPaletteState = photoPaletteState
    ) { contentScale, alignment, state, scrollBar, onLongClick ->
        var myLoadState by remember { mutableStateOf<MyPageState>(MyPageState.Loading) }
        val glideData =
            remember(key1 = sketchImageUri) { sketchUri2GlideModel(sketchImageUri) }
        GlideZoomAsyncImage(
            model = glideData,
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            state = state,
            scrollBar = scrollBar,
            onLongPress = {
                onLongClick.invoke()
            },
            requestBuilderTransform = {
                it.addListener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        myLoadState = MyPageState.Error()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        myLoadState = MyPageState.None
                        return false
                    }
                })
            }
        )

        PageState(state = myLoadState)
    }
}

@Preview
@Composable
private fun GlideZoomAsyncImageSamplePreview() {
    val colorScheme = MaterialTheme.colorScheme
    GlideZoomAsyncImageSample(
        sketchImageUri = newResourceUri(drawableResId = R.drawable.im_placeholder),
        photoPaletteState = remember {
            mutableStateOf(PhotoPalette(colorScheme))
        }
    )
}