package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.util.IntSizeCompat
import com.github.panpf.zoomimage.util.internal.requiredWorkThread
import com.github.panpf.zoomimage.util.internal.toHexString
import com.github.panpf.zoomimage.util.isEmpty
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

class TileBitmapPoolHelper(logger: Logger) {

    companion object {
        private val bitmapPoolLock = Mutex()
    }

    private val logger = logger.newLogger(module = "SubsamplingTileBitmapPoolHelper")
    private val coroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
            logger.e(throwable) { "TileBitmapPoolHelper. CoroutineExceptionHandler: ${throwable.message}" }
        })
    var disallowReuseBitmap = false
    var tileBitmapPool: TileBitmapPool? = null

    private suspend fun <R> withBitmapPoolLock(block: () -> R): R {
        bitmapPoolLock.lock()
        try {
            return block()
        } finally {
            bitmapPoolLock.unlock()
        }
    }

    @WorkerThread
    fun setInBitmapForRegion(
        options: BitmapFactory.Options,
        regionSize: IntSizeCompat,
        imageMimeType: String?,
        imageSize: IntSizeCompat,
        caller: String,
    ): Boolean {
        requiredWorkThread()
        val disallowReuseBitmap = disallowReuseBitmap
        val tileBitmapPool = tileBitmapPool
        if (disallowReuseBitmap || tileBitmapPool == null) {
            return false
        }
        if (regionSize.isEmpty()) {
            logger.e("setInBitmapForRegion:$caller. error. regionSize is empty: $regionSize")
            return false
        }
        val config = options.inPreferredConfig
        if (config?.isAndSupportHardware() == true) {
            logger.d { "setInBitmapForRegion:$caller. error. inPreferredConfig is HARDWARE does not support inBitmap" }
            return false
        }
        if (!isSupportInBitmapForRegion(imageMimeType)) {
            logger.d {
                "setInBitmapForRegion:$caller. error. " +
                        "The current configuration does not support the use of inBitmap in BitmapFactory. " +
                        "imageMimeType=$imageMimeType. " +
                        "For details, please refer to 'DecodeUtils.isSupportInBitmapForRegion()'"
            }
            return false
        }
        val inSampleSize = options.inSampleSize.coerceAtLeast(1)
        val sampledBitmapSize = calculateSampledBitmapSizeForRegion(
            regionSize = regionSize,
            sampleSize = inSampleSize,
            mimeType = imageMimeType,
            imageSize = imageSize
        )
        // BitmapRegionDecoder does not support inMutable, so creates Bitmap
        val width = sampledBitmapSize.width
        val height = sampledBitmapSize.height
        var newCreate = false
        val inBitmap = runBlocking {
            withBitmapPoolLock {
                tileBitmapPool.get(width = width, height = height, config = config)
            }
        } ?: Bitmap.createBitmap(width, height, config).apply { newCreate = true }

        // IllegalArgumentException("Problem decoding into existing bitmap") is thrown when inSampleSize is 0 but inBitmap is not null
        options.inSampleSize = inSampleSize
        options.inBitmap = inBitmap

        val from = if (newCreate) "newCreate" else "fromPool"
        logger.d {
            "setInBitmapForRegion:$caller. successful, $from. " +
                    "regionSize=$regionSize, " +
                    "imageMimeType=$imageMimeType, " +
                    "imageSize=$imageSize. " +
                    "inSampleSize=$inSampleSize, " +
                    "inBitmap=${inBitmap.toHexString()}"
        }
        return true
    }

    @WorkerThread
    fun getOrCreate(width: Int, height: Int, config: Bitmap.Config, caller: String): Bitmap {
        requiredWorkThread()
        val disallowReuseBitmap = disallowReuseBitmap
        val tileBitmapPool = tileBitmapPool
        if (disallowReuseBitmap || tileBitmapPool == null) {
            return Bitmap.createBitmap(width, height, config)
        }

        var newCreate = false
        val bitmap = runBlocking {
            withBitmapPoolLock {
                tileBitmapPool.get(width, height, config)
            }
        } ?: Bitmap.createBitmap(width, height, config).apply { newCreate = true }

        val from = if (newCreate) "newCreate" else "fromPool"
        logger.d {
            "getOrCreate:$caller. $from. width=$width, height=$height, config=$config. bitmap=${this.toHexString()}"
        }
        return bitmap
    }

    fun freeBitmap(bitmap: Bitmap?, caller: String) {
        if (bitmap == null || bitmap.isRecycled) {
            logger.e("freeBitmap:$caller. error, bitmap null or recycled. bitmap=${bitmap?.toHexString()}")
            return
        }

        /*
         * During the asynchronous execution of freeBitmap, if the waiting time is too long and a large number of new bitmaps are generated,
         * a large number of bitmaps will be accumulated in a short period of time, resulting in out of memory
         *
         * The solution is to synchronize the released Bitmap with the produced Bitmap with a lock,
         * so that a large number of new bitmaps are not generated before the old Bitmap is free
         *
         * This bug is triggered when swiping quickly in a SketchZoomImageView
         *
         * Why was this lock put here? Since this problem is caused by asynchronous freeBitmap,
         * it makes more sense to address it here and avoid contaminating other code
         *
         * Execute freeBitmap asynchronously.
         * The BitmapPoolHelper operation was placed in the worker thread，
         * because the main thread would stall if it had to compete with the worker thread for the synchronization lock
         */
        val disallowReuseBitmap = disallowReuseBitmap
        val tileBitmapPool = tileBitmapPool
        coroutineScope.launch(Dispatchers.IO) {
            val success = if (!disallowReuseBitmap && tileBitmapPool != null) {
                withBitmapPoolLock {
                    tileBitmapPool.put(bitmap)
                }
            } else {
                false
            }
            if (success) {
                logger.d { "freeBitmap$$caller. successful. bitmap=${bitmap.toHexString()}" }
            } else {
                bitmap.recycle()
                logger.d { "freeBitmap$$caller. failed, execute recycle. bitmap=${bitmap.toHexString()}" }
            }
        }
    }
}