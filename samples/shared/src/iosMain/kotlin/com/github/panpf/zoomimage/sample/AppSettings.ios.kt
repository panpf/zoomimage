package com.github.panpf.zoomimage.sample

import com.github.panpf.sketch.PlatformContext

actual class AppSettings actual constructor(context: PlatformContext) : BaseAppSettings(context)

actual fun platformSupportedDarkModes(): List<DarkMode> = DarkMode.entries