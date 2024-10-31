package com.github.panpf.zoomimage.core.coil2.test

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import coil.ImageLoader
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.internal.dataToImageSource
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource.WrapperFactory
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
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

class CoreCoilUtilsTest {

    @Test
    fun testDataToImageSource() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = dataToImageSource(context, imageLoader, httpUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    httpUri.toUri()
                )
            )
            assertEquals(
                expected = null,
                actual = dataToImageSource(context, imageLoader, URL(httpUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    httpUri.toHttpUrl()
                )
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = dataToImageSource(context, imageLoader, httpsUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    httpsUri.toUri()
                )
            )
            assertEquals(
                expected = null,
                actual = dataToImageSource(context, imageLoader, URL(httpsUri))
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    httpsUri.toHttpUrl()
                )
            )

            val contentUri = "content://myapp/image.jpg"
            assertEquals(
                expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
                actual = dataToImageSource(context, imageLoader, contentUri)
            )
            assertEquals(
                expected = ContentImageSource(context, contentUri.toUri()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    contentUri.toUri()
                )
            )

            val assetUri = "file:///android_asset/image.jpg"
            val assetFileName = assetUri.toUri().pathSegments.drop(1).joinToString("/")
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = dataToImageSource(context, imageLoader, assetUri)
            )
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    assetUri.toUri()
                )
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, pathUri)
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    pathUri.toUri()
                )
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(context, imageLoader, fileUri)
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    fileUri.toUri()
                )
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = dataToImageSource(
                    context,
                    imageLoader,
                    fileUri.toUri()
                )
            )

            val file = File("/sdcard/image.jpg")
            assertEquals(
                expected = FileImageSource(file).toFactory(),
                actual = dataToImageSource(context, imageLoader, file)
            )

            val resourceId = com.github.panpf.zoomimage.images.R.raw.huge_card
            assertEquals(
                expected = ResourceImageSource(context, resourceId).toFactory(),
                actual = dataToImageSource(context, imageLoader, resourceId)
            )

            val resourceNameUri = "android.resource://${context.packageName}/raw/huge_card"
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceNameUri
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceNameUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceNameUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val resourceIntUri = "android.resource://${context.packageName}/${resourceId}"
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceIntUri
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceIntUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((dataToImageSource(
                    context,
                    imageLoader,
                    resourceIntUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val byteArray = "Hello".toByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = dataToImageSource(context, imageLoader, byteArray)
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = dataToImageSource(context, imageLoader, byteBuffer)
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