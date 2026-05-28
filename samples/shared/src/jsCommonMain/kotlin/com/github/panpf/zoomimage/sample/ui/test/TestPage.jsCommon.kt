package com.github.panpf.zoomimage.sample.ui.test

import com.github.panpf.zoomimage.sample.ui.ExifOrientationTestRoute
import com.github.panpf.zoomimage.sample.ui.GraphicsLayerTestRoute
import com.github.panpf.zoomimage.sample.ui.ImageSourceTestRoute
import com.github.panpf.zoomimage.sample.ui.KeyTestRoute
import com.github.panpf.zoomimage.sample.ui.ModifierZoomTestRoute
import com.github.panpf.zoomimage.sample.ui.MouseTestRoute
import com.github.panpf.zoomimage.sample.ui.OverlayTestRoute
import com.github.panpf.zoomimage.sample.ui.TempTestRoute
import com.github.panpf.zoomimage.sample.ui.ZoomImageSwitchTestRoute

actual fun platformTestItems(): List<Any> = listOf(
    TestGroup("Functions"),
    TestItem("ImageSource", ImageSourceTestRoute),
    TestItem("Exif Orientation", ExifOrientationTestRoute),
    TestItem("Modifier.zoom()", ModifierZoomTestRoute),
    TestItem("KeyZoom", KeyTestRoute),

    TestGroup("UI"),
    TestItem("Overlay", OverlayTestRoute),
    TestItem("Mouse", MouseTestRoute),
    TestItem("Graphics Layer", GraphicsLayerTestRoute),

    TestGroup("Switch"),
    TestItem("ZoomImage (Switch)", ZoomImageSwitchTestRoute),

    TestGroup("Other"),
    TestItem("Temp", TempTestRoute),

    ProjectInfo
)