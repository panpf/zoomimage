package com.github.panpf.zoomimage.core.coil3.android.test

import android.net.Uri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource.Factory
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.images.R
import com.github.panpf.zoomimage.subsampling.AssetImageSource
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.ContentImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.ImageSource.WrapperFactory
import com.github.panpf.zoomimage.subsampling.ResourceImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
import kotlinx.coroutines.test.runTest
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
    fun test() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val modelToImageSource = CoilModelToImageSourceImpl()

            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, httpUri)
            )
            assertEquals(
                expected = Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(httpUri)
                )
            )
            assertEquals(
                expected = Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    httpUri.toUri()
                )
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.modelToImageSource(context, imageLoader, URL(httpUri))
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, httpsUri)
            )
            assertEquals(
                expected = Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(httpsUri)
                )
            )
            assertEquals(
                expected = Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    httpsUri.toUri()
                )
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.modelToImageSource(context, imageLoader, URL(httpsUri))
            )

            val contentUri = "content://myapp/image.jpg"
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, contentUri)
            )
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(contentUri)
                )
            )
            assertEquals(
                expected = ContentImageSource(
                    context,
                    Uri.parse(contentUri)
                ).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    contentUri.toUri()
                )
            )

            val assetUri = "file:///android_asset/image.jpg"
            val assetFileName = assetUri.toUri().pathSegments.drop(1).joinToString("/")
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, assetUri)
            )
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(assetUri)
                )
            )
            assertEquals(
                expected = AssetImageSource(context, assetFileName).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    assetUri.toUri()
                )
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, pathUri)
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(pathUri)
                )
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    pathUri.toUri()
                )
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, fileUri)
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(fileUri)
                )
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    fileUri.toUri()
                )
            )

            val path = "/sdcard/image.jpg".toPath()
            assertEquals(
                expected = FileImageSource(path).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, path)
            )

            val file = File("/sdcard/image.jpg")
            assertEquals(
                expected = FileImageSource(file).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, file)
            )

            val resourceId = R.raw.huge_card
            assertEquals(
                expected = ResourceImageSource(context, resourceId).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, resourceId)
            )

            val resourceNameUri = "android.resource://${context.packageName}/raw/huge_card"
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    resourceNameUri
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context, imageLoader,
                    Uri.parse(resourceNameUri)
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    resourceNameUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val resourceIntUri = "android.resource://${context.packageName}/${resourceId}"
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    resourceIntUri
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    Uri.parse(resourceIntUri)
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )
            assertEquals(
                expected = resourceId,
                actual = ((modelToImageSource.modelToImageSource(
                    context,
                    imageLoader,
                    resourceIntUri.toUri()
                ) as WrapperFactory).imageSource as ResourceImageSource).resId
            )

            val byteArray = "Hello".toByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, byteArray)
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = modelToImageSource.modelToImageSource(context, imageLoader, byteBuffer)
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