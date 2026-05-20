package com.github.panpf.zoomimage.sample

import com.github.panpf.sketch.PlatformContext
import com.github.panpf.zoomimage.sample.util.SettingsStateFlow
import com.github.panpf.zoomimage.sample.util.stringSettingsStateFlow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AppSettings actual constructor(context: PlatformContext) : BaseAppSettings(context) {

    val localPhotosDirPath: SettingsStateFlow<String> by lazy {
        stringSettingsStateFlow(context, "localPhotosDirPath", "")
    }
}