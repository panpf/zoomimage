package com.github.panpf.zoomimage.core.coil2.test

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource.WrapperFactory
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.Buffer
import okio.Path.Companion.toPath
import okio.Source
import okio.Timeout
import okio.buffer
import java.io.File
import java.net.URL
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class CoilModelToImageSourceImplTest {

    @Test
    fun test() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val modelToImageSource = CoilModelToImageSourceImpl(context, imageLoader)

            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(httpUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(httpUri.toUri())
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.dataToImageSource(URL(httpUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(httpUri.toHttpUrl())
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(httpsUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(httpsUri.toUri())
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.dataToImageSource(URL(httpsUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(httpsUri.toHttpUrl())
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
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = modelToImageSource.dataToImageSource(byteArray)
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = modelToImageSource.dataToImageSource(byteBuffer)
            )
        } finally {
            imageLoader.shutdown()
        }
    }

    internal fun ByteBuffer.asSource() = object : Source {
        private val buffer = this@asSource.slice()
        private val len = buffer.capacity()

        override fun close() = Unit

        override fun read(sink: Buffer, byteCount: Long): Long {
            if (buffer.position() == len) return -1
            val pos = buffer.position()
            val newLimit = (pos + byteCount).toInt().coerceAtMost(len)
            buffer.limit(newLimit)
            return sink.write(buffer).toLong()
        }

        override fun timeout() = Timeout.NONE
    }
}