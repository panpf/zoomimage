package com.github.panpf.zoomimage.subsampling.internal

import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import androidx.annotation.WorkerThread
import com.github.panpf.zoomimage.Logger
import com.github.panpf.zoomimage.core.IntSizeCompat
import com.github.panpf.zoomimage.core.internal.isMainThread
import com.github.panpf.zoomimage.core.internal.requiredWorkThread
import com.github.panpf.zoomimage.core.isEmpty
import com.github.panpf.zoomimage.subsampling.TileBitmapPool
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

private val bitmapPoolLock = Mutex()

@WorkerThread
private fun TileBitmapPool.realSetInBitmapForRegion(
    logger: Logger?,
    options: Options,
    regionSize: IntSizeCompat,
    imageMimeType: String?,
    imageSize: IntSizeCompat,
    caller: String? = null,
): Boolean {
    requiredWorkThread()
    if (regionSize.isEmpty()) {
        logger?.e("setInBitmapForRegion. error. regionSize is empty: $regionSize. $caller")
        return false
    }
    if (options.inPreferredConfig?.isAndSupportHardware() == true) {
        logger?.d {
            "setInBitmapForRegion. error. inPreferredConfig is HARDWARE does not support inBitmap. $caller"
        }
        return false
    }
    if (!isSupportInBitmapForRegion(imageMimeType)) {
        logger?.d {
            "setInBitmapForRegion. error. " +
                    "The current configuration does not support the use of inBitmap in BitmapFactory. " +
                    "imageMimeType=$imageMimeType. " +
                    "For details, please refer to 'DecodeUtils.isSupportInBitmapForRegion()'. " +
                    "$caller"
        }
        return false
    }

    val inSampleSize = options.inSampleSize.coerceAtLeast(1)
    val sampledBitmapSize = calculateSampledBitmapSizeForRegion(
        regionSize, inSampleSize, imageMimeType, imageSize
    )
    // BitmapRegionDecoder does not support inMutable, so creates Bitmap
    var newCreate = false
    val inBitmap = get(
        sampledBitmapSize.width, sampledBitmapSize.height, options.inPreferredConfig
    ) ?: Bitmap.createBitmap(
        sampledBitmapSize.width, sampledBitmapSize.height, options.inPreferredConfig
    )!!.apply {
        newCreate = true
    }
    logger?.d {
        "setInBitmapForRegion. successful. " +
                "newCreate $newCreate. " +
                "regionSize=$regionSize, inSampleSize=$inSampleSize, imageSize=$imageSize. " +
                "inBitmap=${inBitmap.toHexString()}. " +
                "$caller"
    }

    // IllegalArgumentException("Problem decoding into existing bitmap") is thrown when inSampleSize is 0 but inBitmap is not null
    options.inSampleSize = inSampleSize
    options.inBitmap = inBitmap
    return true
}

@WorkerThread
fun TileBitmapPool.setInBitmapForRegion(
    logger: Logger?,
    options: Options,
    regionSize: IntSizeCompat,
    imageMimeType: String?,
    imageSize: IntSizeCompat,
    caller: String? = null,
): Boolean {
    return runBlocking {
        bitmapPoolLock.lock()
        try {
            realSetInBitmapForRegion(
                logger = logger,
                options = options,
                regionSize = regionSize,
                imageMimeType = imageMimeType,
                imageSize = imageSize,
                caller = caller
            )
        } finally {
            bitmapPoolLock.unlock()
        }
    }
}

@WorkerThread
private fun TileBitmapPool.realGetOrCreate(
    logger: Logger?,
    width: Int,
    height: Int,
    config: Bitmap.Config,
    caller: String? = null,
): Bitmap {
    requiredWorkThread()
    return get(width, height, config) ?: Bitmap.createBitmap(width, height, config)
        .apply {
            logger?.d {
                "getOrCreate. new. ${this.toHexString()}. $caller"
            }
        }
}

@WorkerThread
fun TileBitmapPool.getOrCreate(
    logger: Logger?,
    width: Int,
    height: Int,
    config: Bitmap.Config,
    caller: String? = null,
): Bitmap {
    return runBlocking {
        bitmapPoolLock.lock()
        try {
            realGetOrCreate(logger, width, height, config, caller)
        } finally {
            bitmapPoolLock.unlock()
        }
    }
}

@WorkerThread
private fun TileBitmapPool.realFreeBitmap(
    logger: Logger?,
    bitmap: Bitmap,
    caller: String? = null
) {
    requiredWorkThread()
    val success = put(bitmap)
    if (success) {
        logger?.d {
            "freeBitmap. successful. $caller. ${bitmap.toHexString()}"
        }
    } else {
        bitmap.recycle()
        logger?.d {
            "freeBitmap. failed. execute recycle. $caller. ${bitmap.toHexString()}"
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun TileBitmapPool.freeBitmap(
    logger: Logger?,
    bitmap: Bitmap?,
    caller: String? = null,
) {
    if (bitmap == null || bitmap.isRecycled) {
        logger?.w {
            "freeBitmap. error. bitmap null or recycled. $caller. ${bitmap?.toHexString()}"
        }
        return
    }

    if (isMainThread()) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
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
             */
            bitmapPoolLock.lock()
            try {
                // Execute freeBitmap asynchronously.
                // The BitmapPoolHelper operation was placed in the worker thread，
                // because the main thread would stall if it had to compete with the worker thread for the synchronization lock
                withContext(Dispatchers.IO) {
                    realFreeBitmap(logger, bitmap, caller)
                }
            } finally {
                bitmapPoolLock.unlock()
            }
        }
    } else {
        realFreeBitmap(logger, bitmap, caller)
    }
}