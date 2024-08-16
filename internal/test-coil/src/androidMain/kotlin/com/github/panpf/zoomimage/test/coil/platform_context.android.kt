package com.github.panpf.zoomimage.test.coil

import androidx.test.platform.app.InstrumentationRegistry
import coil3.PlatformContext

actual val platformContext: PlatformContext
    get() = InstrumentationRegistry.getInstrumentation().context