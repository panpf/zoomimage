package com.github.panpf.zoomimage.sample.util

actual val Platform.Companion.current: Platform
    get() = Platform.WasmJs