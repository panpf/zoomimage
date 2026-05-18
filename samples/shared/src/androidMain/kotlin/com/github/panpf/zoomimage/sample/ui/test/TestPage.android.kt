package com.github.panpf.zoomimage.sample.ui.test

import com.github.panpf.zoomimage.sample.ui.TelephotoSwitchTestRoute
import com.github.panpf.zoomimage.sample.ui.TempAndroidTestRoute

actual fun platformTestItems(): List<TestItem> =
    listOf(
        TestItem("Telephoto (Switch)", TelephotoSwitchTestRoute),
        TestItem("Temp (Android)", TempAndroidTestRoute),
    )