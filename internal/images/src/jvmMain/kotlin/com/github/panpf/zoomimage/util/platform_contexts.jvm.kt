package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toPath

actual fun MyPlatformContext.appCacheDirectory(): Path? {
    return AppDirs.getCacheDir("ZoomImage Sample").toPath()
}