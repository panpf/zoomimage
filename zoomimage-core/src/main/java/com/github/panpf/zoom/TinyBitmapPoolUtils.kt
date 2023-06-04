package com.github.panpf.zoom

import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import androidx.annotation.WorkerThread
import com.github.panpf.zoom.internal.calculateSampledBitmapSizeForRegion
import com.github.panpf.zoom.internal.isAndSupportHardware
import com.github.panpf.zoom.internal.isMainThread
import com.github.panpf.zoom.internal.isSupportInBitmapForRegion
import com.github.panpf.zoom.internal.logString
import com.github.panpf.zoom.internal.requiredWorkThread
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

private const val MODULE = "BitmapPoolUtils"

private val bitmapPoolLock = Mutex()

//@WorkerThread
//private fun TinyBitmapPool.realSetInBitmap(
//    logger: Logger?,
//    options: Options,
//    imageSize: Size,
//    imageMimeType: String?,
//    disallowReuseBitmap: Boolean = false,
//    caller: String? = null,
//): Boolean {
//    requiredWorkThread()
//    if (disallowReuseBitmap) {
//        logger?.d(MODULE) {
//            "setInBitmap. disallowReuseBitmap. imageSize=$imageSize, imageMimeType=$imageMimeType. $caller"
//        }
//        return false
//    }
//    if (imageSize.isEmpty) {
//        logger?.e(MODULE, "setInBitmap. error. imageSize is empty: $imageSize. $caller")
//        return false
//    }
//    if (options.inPreferredConfig?.isAndSupportHardware() == true) {
//        logger?.d(MODULE) {
//            "setInBitmap. error. inPreferredConfig is HARDWARE does not support inBitmap. $caller"
//        }
//        return false
//    }
//
//    // Going here can make the decoded Bitmap mutable so that it can be put into BitmapPoolHelper
//    options.inMutable = true
//
//    val inSampleSize = options.inSampleSize.coerceAtLeast(1)
//    if (!isSupportInBitmap(imageMimeType, inSampleSize)) {
//        logger?.d(MODULE) {
//            "setInBitmap. error. " +
//                    "The current configuration does not support the use of inBitmap in BitmapFactory. " +
//                    "imageMimeType=$imageMimeType, inSampleSize=${options.inSampleSize}. " +
//                    "For details, please refer to 'DecodeUtils.isSupportInBitmap()'. " +
//                    "$caller"
//        }
//        return false
//    }
//
//    val sampledBitmapSize =
//        calculateSampledBitmapSize(imageSize, inSampleSize, imageMimeType)
//    val inBitmap: Bitmap? = get(
//        width = sampledBitmapSize.width,
//        height = sampledBitmapSize.height,
//        config = options.inPreferredConfig
//    )
//    if (inBitmap != null) {
//        logger?.d(MODULE) {
//            "setInBitmap. successful. " +
//                    "imageSize=$imageSize, inSampleSize=$inSampleSize, imageMimeType=$imageMimeType. " +
//                    "inBitmap=${inBitmap.logString}. " +
//                    "$caller"
//        }
//    } else {
//        logger?.d(MODULE) {
//            "setInBitmap. failed. " +
//                    "imageSize=$imageSize, inSampleSize=$inSampleSize, imageMimeType=$imageMimeType. " +
//                    "$caller"
//        }
//    }
//
//    // IllegalArgumentException("Problem decoding into existing bitmap") is thrown when inSampleSize is 0 but inBitmap is not null
//    options.inSampleSize = inSampleSize
//    options.inBitmap = inBitmap
//    return inBitmap != null
//}

//@WorkerThread
//fun TinyBitmapPool.setInBitmap(
//    logger: Logger?,
//    options: Options,
//    imageSize: Size,
//    imageMimeType: String?,
//    disallowReuseBitmap: Boolean = false,
//    caller: String? = null,
//): Boolean {
//    return runBlocking {
//        bitmapPoolLock.lock()
//        try {
//            realSetInBitmap(logger, options, imageSize, imageMimeType, disallowReuseBitmap, caller)
//        } finally {
//            bitmapPoolLock.unlock()
//        }
//    }
//}

@WorkerThread
private fun TinyBitmapPool.realSetInBitmapForRegion(
    logger: Logger?,
    options: Options,
    regionSize: Size,
    imageMimeType: String?,
    imageSize: Size,
    disallowReuseBitmap: Boolean = false,
    caller: String? = null,
): Boolean {
    requiredWorkThread()
    if (disallowReuseBitmap) {
        logger?.d(MODULE) {
            "setInBitmapForRegion. disallowReuseBitmap. imageSize=$imageSize, imageMimeType=$imageMimeType. $caller"
        }
        return false
    }
    if (regionSize.isEmpty) {
        logger?.e(MODULE, "setInBitmapForRegion. error. regionSize is empty: $regionSize. $caller")
        return false
    }
    if (options.inPreferredConfig?.isAndSupportHardware() == true) {
        logger?.d(MODULE) {
            "setInBitmapForRegion. error. inPreferredConfig is HARDWARE does not support inBitmap. $caller"
        }
        return false
    }
    if (!isSupportInBitmapForRegion(imageMimeType)) {
        logger?.d(MODULE) {
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
    logger?.d(MODULE) {
        "setInBitmapForRegion. successful. " +
                "newCreate $newCreate. " +
                "regionSize=$regionSize, inSampleSize=$inSampleSize, imageSize=$imageSize. " +
                "inBitmap=${inBitmap.logString}. " +
                "$caller"
    }

    // IllegalArgumentException("Problem decoding into existing bitmap") is thrown when inSampleSize is 0 but inBitmap is not null
    options.inSampleSize = inSampleSize
    options.inBitmap = inBitmap
    return true
}

@WorkerThread
fun TinyBitmapPool.setInBitmapForRegion(
    logger: Logger?,
    options: Options,
    regionSize: Size,
    imageMimeType: String?,
    imageSize: Size,
    disallowReuseBitmap: Boolean = false,
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
                disallowReuseBitmap = disallowReuseBitmap,
                caller = caller
            )
        } finally {
            bitmapPoolLock.unlock()
        }
    }
}

@WorkerThread
private fun TinyBitmapPool.realGetOrCreate(
    logger: Logger?,
    width: Int,
    height: Int,
    config: Bitmap.Config,
    disallowReuseBitmap: Boolean = false,
    caller: String? = null,
): Bitmap {
    requiredWorkThread()
    if (disallowReuseBitmap) {
        return Bitmap.createBitmap(width, height, config).apply {
            logger?.d(MODULE) {
                "getOrCreate. new disallowReuseBitmap. ${this.logString}. $caller"
            }
        }
    }
    return get(width, height, config) ?: Bitmap.createBitmap(width, height, config)
        .apply {
            logger?.d(MODULE) {
                "getOrCreate. new . ${this.logString}. $caller"
            }
        }
}

@WorkerThread
fun TinyBitmapPool.getOrCreate(
    logger: Logger?,
    width: Int,
    height: Int,
    config: Bitmap.Config,
    disallowReuseBitmap: Boolean = false,
    caller: String? = null,
): Bitmap {
    return runBlocking {
        bitmapPoolLock.lock()
        try {
            realGetOrCreate(logger, width, height, config, disallowReuseBitmap, caller)
        } finally {
            bitmapPoolLock.unlock()
        }
    }
}

@WorkerThread
private fun TinyBitmapPool.realFreeBitmap(logger: Logger?, bitmap: Bitmap, caller: String? = null) {
    requiredWorkThread()
    val success = put(bitmap)
    if (success) {
        logger?.d(MODULE) {
            "freeBitmap. successful. $caller. ${bitmap.logString}"
        }
    } else {
        bitmap.recycle()
        logger?.d(MODULE) {
            "freeBitmap. failed. execute recycle. $caller. ${bitmap.logString}"
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun TinyBitmapPool.freeBitmap(
    logger: Logger?,
    bitmap: Bitmap?,
    disallowReuseBitmap: Boolean = false,
    caller: String? = null,
) {
    if (bitmap == null || bitmap.isRecycled) {
        logger?.w(MODULE) {
            "freeBitmap. error. bitmap null or recycled. $caller. ${bitmap?.logString}"
        }
        return
    }
    if (disallowReuseBitmap) {
        bitmap.recycle()
        logger?.d(MODULE) {
            "freeBitmap. disallowReuseBitmap. execute recycle. $caller. ${bitmap.logString}"
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