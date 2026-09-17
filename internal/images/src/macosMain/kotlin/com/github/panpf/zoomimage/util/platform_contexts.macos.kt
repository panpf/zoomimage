package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Return the application's cache directory.
 *
 * @see com.github.panpf.sketch.core.ios.test.util.PlatformContextsIosTest.testAppCacheDirectory
 */
actual fun MyPlatformContext.appCacheDirectory(): Path? {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    val cachesDirectory = (paths.firstOrNull() as? String)?.toPath()
    return cachesDirectory?.resolve("ZoomImageSample")
}