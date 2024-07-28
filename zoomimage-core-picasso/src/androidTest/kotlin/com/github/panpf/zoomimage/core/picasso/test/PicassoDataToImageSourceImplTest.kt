package com.github.panpf.zoomimage.core.picasso.test

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.picasso.PicassoDataToImageSourceImpl
import com.github.panpf.zoomimage.picasso.PicassoHttpImageSource
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource.WrapperFactory
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import com.squareup.picasso.Picasso
import okio.Path.Companion.toPath
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals

class PicassoDataToImageSourceImplTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Picasso.get()
        val modelToImageSource = PicassoDataToImageSourceImpl(context)

        val httpUri = "http://www.example.com/image.jpg"
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(httpUri)
        )
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(httpUri.toUri())
        )
        assertEquals(
            expected = null,
            actual = modelToImageSource.dataToImageSource(URL(httpUri))
        )

        val httpsUri = "https://www.example.com/image.jpg"
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpsUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(httpsUri)
        )
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpsUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(httpsUri.toUri())
        )
        assertEquals(
            expected = null,
            actual = modelToImageSource.dataToImageSource(URL(httpsUri))
        )

        val contentUri = "content://myapp/image.jpg"
        assertEquals(
            expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(contentUri)
        )
        assertEquals(
            expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
            actual = modelToImageSource.dataToImageSource(contentUri.toUri())
        )

        val assetUri = "file:///android_asset/image.jpg"
        val assetFileName = assetUri.toUri().pathSegments.drop(1).joinToString("/")
        assertEquals(
            expected = AssetImageSource(context, assetFileName).toFactory(),
            actual = modelToImageSource.dataToImageSource(assetUri)
        )
        assertEquals(
            expected = AssetImageSource(context, assetFileName).toFactory(),
            actual = modelToImageSource.dataToImageSource(assetUri.toUri())
        )

        val pathUri = "/sdcard/image.jpg"
        assertEquals(
            expected = FileImageSource(pathUri.toPath()).toFactory(),
            actual = modelToImageSource.dataToImageSource(pathUri)
        )
        assertEquals(
            expected = FileImageSource(pathUri.toPath()).toFactory(),
            actual = modelToImageSource.dataToImageSource(pathUri.toUri())
        )

        val fileUri = "file:///sdcard/image.jpg"
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = modelToImageSource.dataToImageSource(fileUri)
        )
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = modelToImageSource.dataToImageSource(fileUri.toUri())
        )
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = modelToImageSource.dataToImageSource(fileUri.toUri())
        )

        val file = File("/sdcard/image.jpg")
        assertEquals(
            expected = FileImageSource(file).toFactory(),
            actual = modelToImageSource.dataToImageSource(file)
        )

        val resourceId = com.github.panpf.zoomimage.images.R.raw.huge_card
        assertEquals(
            expected = ResourceImageSource(context, resourceId).toFactory(),
            actual = modelToImageSource.dataToImageSource(resourceId)
        )

        val resourceNameUri = "android.resource://${context.packageName}/raw/huge_card"
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceNameUri) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceNameUri.toUri()) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceNameUri.toUri()) as WrapperFactory).imageSource as ResourceImageSource).resId
        )

        val resourceIntUri = "android.resource://${context.packageName}/${resourceId}"
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceIntUri) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceIntUri.toUri()) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((modelToImageSource.dataToImageSource(resourceIntUri.toUri()) as WrapperFactory).imageSource as ResourceImageSource).resId
        )

        val byteArray = "Hello".toByteArray()
        assertEquals(
            expected = null,
            actual = modelToImageSource.dataToImageSource(byteArray)
        )
    }
}