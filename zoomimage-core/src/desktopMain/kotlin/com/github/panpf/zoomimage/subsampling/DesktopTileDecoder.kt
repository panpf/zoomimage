package com.github.panpf.zoomimage.subsampling

import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.util.IntRectCompat
import com.github.panpf.zoomimage.util.Logger
import com.github.panpf.zoomimage.util.internal.quietClose
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.Closeable
import java.util.LinkedList
import javax.imageio.ImageIO
import javax.imageio.ImageReader

class DesktopTileDecoder constructor(
    logger: Logger,
    private val imageSource: ImageSource,
    override val imageInfo: ImageInfo
) : TileDecoder {

    override val exifOrientation: ExifOrientation? = null

    private val logger = logger.newLogger(module = "TileDecoder")
    private var destroyed = false
    private val decoderPool = LinkedList<ImageReader>()
//    private val addedImageSize = exifOrientation?.addToSize(imageInfo.size) ?: imageInfo.size

    @WorkerThread
    override fun decode(srcRect: IntRectCompat, sampleSize: Int): TileBitmap? {
//        requiredWorkThread()
        if (destroyed) return null
        val bitmap = useDecoder { decoder ->
            decodeRegion(decoder, srcRect, sampleSize)
        } ?: return null
        return applyExifOrientation(DesktopTileBitmap(bitmap))
    }

    override fun destroy(caller: String) {
//        requiredMainThread()
        if (destroyed) return
        destroyed = true
        logger.d { "destroy:$caller. '${imageSource.key}'" }
        synchronized(decoderPool) {
            decoderPool.forEach {
                it.recycle()
            }
            decoderPool.clear()
        }
    }

    @WorkerThread
    private fun decodeRegion(
        imageReader: ImageReader,
        srcRect: IntRectCompat,
        inSampleSize: Int
    ): BufferedImage? {
//        requiredWorkThread()
        val imageSize = imageInfo.size
        val newSrcRect = exifOrientation?.addToRect(srcRect, imageSize) ?: srcRect
        val readParam = imageReader.defaultReadParam.apply {
            sourceRegion = Rectangle(
                /* x = */ newSrcRect.left,
                /* y = */ newSrcRect.top,
                /* width = */ newSrcRect.width,
                /* height = */ newSrcRect.height
            )
            setSourceSubsampling(inSampleSize, inSampleSize, 0, 0)
            // todo When the inSampleSize is greater than 1, there is a problem with the color of the decoded picture, similar to zebra stripe, with one line of light color and one line of dark color
//            setDestinationType(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_BGR))
//            destination = BufferedImage(
//                /* width = */ newSrcRect.width / inSampleSize,
//                /* height = */ newSrcRect.height / inSampleSize,
//                /* imageType = */ BufferedImage.TYPE_INT_ARGB
//            )
        }
        return imageReader.read(0, readParam)
    }

    @WorkerThread
    private fun useDecoder(block: (decoder: ImageReader) -> BufferedImage?): BufferedImage? {
        synchronized(decoderPool) {
            if (destroyed) {
                return null
            }
        }

        var imageReader: ImageReader? = synchronized(decoderPool) {
            decoderPool.poll()
        }
        if (imageReader == null) {
            val inputStream = imageSource.openInputStream().getOrNull()?.buffered()
            val imageStream = ImageIO.createImageInputStream(inputStream)
            imageReader = ImageIO.getImageReaders(imageStream).next().apply {
//                setInput(imageStream, true, true)
                setInput(imageStream)
            }
        }
        if (imageReader == null) {
            return null
        }

        val bufferedImage = block(imageReader)

        synchronized(decoderPool) {
            if (destroyed) {
                imageReader.recycle()
            } else {
                decoderPool.add(imageReader)
            }
        }

        return bufferedImage
    }

    @WorkerThread
    private fun applyExifOrientation(tileBitmap: TileBitmap): TileBitmap {
//        requiredWorkThread()
        val newBitmap = exifOrientation
            ?.applyToTileBitmap(null, tileBitmap)
            ?: tileBitmap
        return if (newBitmap !== tileBitmap) {
//            if (tileBitmapReuseHelper != null) {
//                tileBitmapReuseHelper.freeTileBitmap(tileBitmap, "applyExifOrientation")
//            } else {
            tileBitmap.recycle()
//            }
            newBitmap
        } else {
            tileBitmap
        }
    }

    private fun ImageReader.recycle() {
        dispose()
        (input as Closeable).quietClose()
    }

    override fun toString(): String {
        return "DesktopTileDecoder(imageSource='${imageSource.key}', imageInfo=$imageInfo, exifOrientation=$exifOrientation)"
    }
}