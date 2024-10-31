package com.github.panpf.zoomimage.core.picasso.test.internal

import android.graphics.Bitmap
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.zoomimage.picasso.PicassoHttpImageSource
import com.github.panpf.zoomimage.picasso.internal.dataToImageSource
import com.github.panpf.zoomimage.picasso.internal.toHexString
import com.github.panpf.zoomimage.picasso.internal.toLogString
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource.WrapperFactory
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import com.squareup.picasso.Picasso
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PicassoOtherUtilsTest {

    @Test
    fun testToHexString() {
        val any1 = Any()
        val any2 = Any()
        assertEquals(
            expected = any1.hashCode().toString(16),
            actual = any1.toHexString()
        )
        assertEquals(
            expected = any2.hashCode().toString(16),
            actual = any2.toHexString()
        )
        assertNotEquals(
            illegal = any1.toHexString(),
            actual = any2.toHexString()
        )
    }

    @Test
    fun testToLogString() {
        Bitmap.createBitmap(110, 210, Bitmap.Config.ARGB_8888).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(110x210,ARGB_8888)",
                this.toLogString()
            )
        }

        Bitmap.createBitmap(210, 110, Bitmap.Config.RGB_565).apply {
            assertEquals(
                "Bitmap@${Integer.toHexString(this.hashCode())}(210x110,RGB_565)",
                this.toLogString()
            )
        }
    }

    @Test
    fun testDataToImageSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val glide = Picasso.get()

        val httpUri = "http://www.example.com/image.jpg"
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpUri.toUri()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), httpUri)
        )
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpUri.toUri()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), httpUri.toUri())
        )
        assertEquals(
            expected = null,
            actual = dataToImageSource(context, Picasso.get(), URL(httpUri))
        )

        val httpsUri = "https://www.example.com/image.jpg"
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpsUri.toUri()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), httpsUri)
        )
        assertEquals(
            expected = PicassoHttpImageSource(glide, httpsUri.toUri()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), httpsUri.toUri())
        )
        assertEquals(
            expected = null,
            actual = dataToImageSource(context, Picasso.get(), URL(httpsUri))
        )

        val contentUri = "content://myapp/image.jpg"
        assertEquals(
            expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), contentUri)
        )
        assertEquals(
            expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
            actual = dataToImageSource(
                context,
                Picasso.get(),
                contentUri.toUri()
            )
        )

        val assetUri = "file:///android_asset/image.jpg"
        val assetFileName = assetUri.toUri().pathSegments.drop(1).joinToString("/")
        assertEquals(
            expected = AssetImageSource(context, assetFileName).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), assetUri)
        )
        assertEquals(
            expected = AssetImageSource(context, assetFileName).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), assetUri.toUri())
        )

        val pathUri = "/sdcard/image.jpg"
        assertEquals(
            expected = FileImageSource(pathUri.toPath()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), pathUri)
        )
        assertEquals(
            expected = FileImageSource(pathUri.toPath()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), pathUri.toUri())
        )

        val fileUri = "file:///sdcard/image.jpg"
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), fileUri)
        )
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), fileUri.toUri())
        )
        assertEquals(
            expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), fileUri.toUri())
        )

        val file = File("/sdcard/image.jpg")
        assertEquals(
            expected = FileImageSource(file).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), file)
        )

        val resourceId = com.github.panpf.zoomimage.images.R.raw.huge_card
        assertEquals(
            expected = ResourceImageSource(context, resourceId).toFactory(),
            actual = dataToImageSource(context, Picasso.get(), resourceId)
        )

        val resourceNameUri = "android.resource://${context.packageName}/raw/huge_card"
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceNameUri
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceNameUri.toUri()
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceNameUri.toUri()
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )

        val resourceIntUri = "android.resource://${context.packageName}/${resourceId}"
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceIntUri
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceIntUri.toUri()
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )
        assertEquals(
            expected = resourceId,
            actual = ((dataToImageSource(
                context,
                Picasso.get(),
                resourceIntUri.toUri()
            ) as WrapperFactory).imageSource as ResourceImageSource).resId
        )

        val byteArray = "Hello".toByteArray()
        assertEquals(
            expected = null,
            actual = dataToImageSource(context, Picasso.get(), byteArray)
        )
    }
}