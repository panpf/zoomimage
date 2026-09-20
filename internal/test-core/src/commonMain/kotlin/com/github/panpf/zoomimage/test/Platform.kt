package com.github.panpf.zoomimage.test

enum class Platform {
    Android,
    iOS,
    macOS,
    Jvm,
    Web, ;

    companion object
}

expect val Platform.Companion.current: Platform
