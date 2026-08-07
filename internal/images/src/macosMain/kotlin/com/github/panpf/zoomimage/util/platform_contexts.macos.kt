package com.github.panpf.zoomimage.util

import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * Return the application's cache directory.
 *
 * @see com.github.panpf.sketch.core.ios.test.util.PlatformContextsIosTest.testAppCacheDirectory
 */
actual fun MyPlatformContext.appCacheDirectory(): Path? {
    val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
    val cachesDirectory = (paths.firstOrNull() as? String)?.toPath() ?: return null
    val applicationId = NSBundle.mainBundle.bundleIdentifier
        ?.takeIf { it.isNotBlank() }
        ?: NSProcessInfo.processInfo.processName.takeIf { it.isNotBlank() }
        ?: "com.github.panpf.zoomimage"
    return cachesDirectory.resolve(applicationId)
}