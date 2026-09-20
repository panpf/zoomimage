package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toPath

/**
 * Return the application's cache directory.
 *
 * @see com.github.panpf.sketch.core.ios.test.util.PlatformContextsIosTest.testAppCacheDirectory
 */
actual fun MyPlatformContext.appCacheDirectory(): Path? {
    return AppDirs.getCacheDir("ZoomImage Sample").toPath()
}