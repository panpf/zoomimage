package com.github.panpf.zoomimage.test

enum class Platform {
    Android,
    iOS,
    macOS,
    Desktop,
    Web, ;

    companion object
}

expect val Platform.Companion.current: Platform
