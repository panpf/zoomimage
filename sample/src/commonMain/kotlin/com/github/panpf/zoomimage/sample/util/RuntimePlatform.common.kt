package com.github.panpf.zoomimage.sample.util

enum class RuntimePlatform {
    Android,
    JvmDesktop,
    Js,
    Ios,
    WasmJs,
}

expect val runtimePlatformInstance: RuntimePlatform

fun RuntimePlatform.isMobile(): Boolean =
    this == RuntimePlatform.Android || this == RuntimePlatform.Ios