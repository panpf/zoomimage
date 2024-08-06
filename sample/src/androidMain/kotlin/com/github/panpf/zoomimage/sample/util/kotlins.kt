package com.github.panpf.zoomimage.sample.util

inline fun <T> T.letIf(predicate: Boolean, block: (T) -> T): T {
    return if (predicate) block(this) else this
}