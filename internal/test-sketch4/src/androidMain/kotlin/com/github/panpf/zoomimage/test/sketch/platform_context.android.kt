package com.github.panpf.zoomimage.test.sketch

import androidx.test.platform.app.InstrumentationRegistry
import com.github.panpf.sketch.PlatformContext

actual val platformContext: PlatformContext
    get() = InstrumentationRegistry.getInstrumentation().context