package com.github.panpf.zoomimage.sample

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow

actual class AppSettings actual constructor(context: PlatformContext) : BaseAppSettings(context) {

    val localPhotosDirPath: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "localPhotosDirPath", "")
    }
}

actual fun platformSupportedDarkModes(): List<DarkMode> = DarkMode.entries