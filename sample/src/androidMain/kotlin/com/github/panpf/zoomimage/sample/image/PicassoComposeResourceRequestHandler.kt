package com.github.panpf.zoomimage.sample.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.github.panpf.sketch.fetch.isComposeResourceUri
import com.github.panpf.sketch.util.toUri
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import java.io.ByteArrayInputStream
import java.io.IOException
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class PicassoComposeResourceRequestHandler : RequestHandler() {

    override fun canHandleRequest(data: Request?): Boolean {
        data ?: return false
        return isComposeResourceUri(data.uri?.toString().orEmpty().toUri())
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): Result {
        return Result(decodeResource(request), LoadedFrom.DISK)
    }

    @OptIn(InternalResourceApi::class)
    private fun decodeResource(data: Request): Bitmap {
        val uri = data.uri!!
        val resourcePath = uri.pathSegments.drop(1).joinToString("/")
        val bytes = runBlocking {
            readResourceBytes(resourcePath)
        }
        val options = createBitmapOptions(data)
        if (requiresInSampleSize(options)) {
            BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, options)
            calculateInSampleSize(data.targetWidth, data.targetHeight, options!!, data)
        }
        val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, options)!!
        return bitmap
    }

    /**
     * Lazily create [BitmapFactory.Options] based in given
     * [Request], only instantiating them if needed.
     */
    fun createBitmapOptions(data: Request): BitmapFactory.Options? {
        val justBounds = data.hasSize()
        val hasConfig = data.config != null
        var options: BitmapFactory.Options? = null
        if (justBounds || hasConfig || data.purgeable) {
            options = BitmapFactory.Options()
            options.inJustDecodeBounds = justBounds
//            options.inInputShareable = data.purgeable
//            options.inPurgeable = data.purgeable
            if (hasConfig) {
                options.inPreferredConfig = data.config
            }
        }
        return options
    }

    fun requiresInSampleSize(options: BitmapFactory.Options?): Boolean {
        return options != null && options.inJustDecodeBounds
    }

    fun calculateInSampleSize(
        reqWidth: Int, reqHeight: Int, options: BitmapFactory.Options,
        request: Request
    ) {
        calculateInSampleSize(
            reqWidth, reqHeight, options.outWidth, options.outHeight, options,
            request
        )
    }

    fun calculateInSampleSize(
        reqWidth: Int, reqHeight: Int, width: Int, height: Int,
        options: BitmapFactory.Options, request: Request
    ) {
        var sampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio: Int
            val widthRatio: Int
            if (reqHeight == 0) {
                sampleSize = floor((width.toFloat() / reqWidth.toFloat()).toDouble()).toInt()
            } else if (reqWidth == 0) {
                sampleSize = floor((height.toFloat() / reqHeight.toFloat()).toDouble()).toInt()
            } else {
                heightRatio = floor((height.toFloat() / reqHeight.toFloat()).toDouble()).toInt()
                widthRatio = floor((width.toFloat() / reqWidth.toFloat()).toDouble()).toInt()
                sampleSize = if (request.centerInside
                ) max(
                    heightRatio.toDouble(),
                    widthRatio.toDouble()
                ).toInt() else min(heightRatio.toDouble(), widthRatio.toDouble())
                    .toInt()
            }
        }
        options.inSampleSize = sampleSize
        options.inJustDecodeBounds = false
    }
}