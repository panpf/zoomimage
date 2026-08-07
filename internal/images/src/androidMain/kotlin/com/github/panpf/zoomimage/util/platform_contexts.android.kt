package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toOkioPath

actual fun MyPlatformContext.appCacheDirectory(): Path? {
    val appCacheDirectory = externalCacheDir ?: cacheDir
    return appCacheDirectory.toOkioPath()
}