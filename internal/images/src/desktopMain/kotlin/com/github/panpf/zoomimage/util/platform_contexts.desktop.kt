package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toOkioPath

actual fun MyPlatformContext.appCacheDirectory(): Path? {
    return AppDirs.getCacheDir("ZoomImageSample").toOkioPath()
}