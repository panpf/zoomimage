package com.github.panpf.zoomimage.util

import okio.Closeable


@PublishedApi // Used by extension modules.
internal fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {}
}