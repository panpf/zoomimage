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
import com.github.panpf.zoomimage.sample.ui.model.Photo
import com.github.panpf.zoomimage.sample.util.sketchUri2GlideModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideZoomAsyncImageSample(
    photo: Photo,
    photoPaletteState: MutableState<PhotoPalette>
) {
    BaseZoomImageSample(
        photo = photo,
        photoPaletteState = photoPaletteState
    ) { contentScale, alignment, state, scrollBar, onLongClick ->
        var myLoadState by remember { mutableStateOf<MyPageState>(MyPageState.Loading) }
        val glideData = remember(key1 = photo) { sketchUri2GlideModel(photo.originalUrl) }
        GlideZoomAsyncImage(
            model = glideData,
            contentDescription = "view image",
            contentScale = contentScale,
            alignment = alignment,
            modifier = Modifier.fillMaxSize(),
            zoomState = state,
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
    val photo = remember {
        val sketchImageUri = newResourceUri(drawableResId = R.drawable.im_placeholder)
        Photo(sketchImageUri)
    }
    GlideZoomAsyncImageSample(
        photo = photo,
        photoPaletteState = remember {
            mutableStateOf(PhotoPalette(colorScheme))
        }
    )
}