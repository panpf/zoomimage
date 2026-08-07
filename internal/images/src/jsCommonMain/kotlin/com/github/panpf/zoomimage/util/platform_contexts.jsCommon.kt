package com.github.panpf.zoomimage.util

import okio.Path

/**
 * Return the application's cache directory.
 *
 * @see com.github.panpf.sketch.core.jscommon.test.util.PlatformContextsJsCommonTest.testAppCacheDirectory
 */
actual fun MyPlatformContext.appCacheDirectory(): Path? = null