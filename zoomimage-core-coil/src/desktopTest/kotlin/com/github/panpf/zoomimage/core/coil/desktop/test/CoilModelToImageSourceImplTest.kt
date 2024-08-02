package com.github.panpf.zoomimage.core.coil.desktop.test

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.toUri
import com.github.panpf.zoomimage.coil.CoilHttpImageSource
import com.github.panpf.zoomimage.coil.CoilModelToImageSourceImpl
import com.github.panpf.zoomimage.subsampling.ByteArrayImageSource
import com.github.panpf.zoomimage.subsampling.FileImageSource
import com.github.panpf.zoomimage.subsampling.toFactory
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
        val context = PlatformContext.INSTANCE
        val imageLoader = ImageLoader.Builder(context).build()
        try {
            val modelToImageSource = CoilModelToImageSourceImpl()

            val httpUri = "http://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpUri.toUri())
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.dataToImageSource(context, imageLoader, URL(httpUri))
            )

            val httpsUri = "https://www.example.com/image.jpg"
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpsUri)
            )
            assertEquals(
                expected = CoilHttpImageSource.Factory(context, imageLoader, httpsUri),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, httpsUri.toUri())
            )
            assertEquals(
                expected = null,
                actual = modelToImageSource.dataToImageSource(context, imageLoader, URL(httpsUri))
            )

            val pathUri = "/sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, pathUri)
            )
            assertEquals(
                expected = FileImageSource(pathUri.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, pathUri.toUri())
            )

            val fileUri = "file:///sdcard/image.jpg"
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, fileUri)
            )
            assertEquals(
                expected = FileImageSource(fileUri.toUri().path!!.toPath()).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, fileUri.toUri())
            )

            val path = "/sdcard/image.jpg".toPath()
            assertEquals(
                expected = FileImageSource(path).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, path)
            )

            val file = File("/sdcard/image.jpg")
            assertEquals(
                expected = FileImageSource(file).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, file)
            )

            val byteArray = "Hello".toByteArray()
            assertEquals(
                expected = ByteArrayImageSource(byteArray).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, byteArray)
            )

            val byteBuffer = ByteBuffer.wrap("Hello".toByteArray())
            assertEquals(
                expected = ByteArrayImageSource(
                    byteBuffer.asSource().buffer().use { it.readByteArray() }).toFactory(),
                actual = modelToImageSource.dataToImageSource(context, imageLoader, byteBuffer)
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