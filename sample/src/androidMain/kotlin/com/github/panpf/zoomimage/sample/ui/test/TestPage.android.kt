package com.github.panpf.zoomimage.sample.ui.test


actual fun testItems(): List<TestItem> = listOf(
    TestItem("ImageSource", ImageSourceTestScreen()),
    TestItem("Exif Orientation", ExifOrientationTestScreen()),
    TestItem("Graphics Layer", GraphicsLayerTestScreen()),
    TestItem("Modifier.zoom()", ModifierZoomTestScreen()),
    TestItem("Mouse", MouseTestScreen()),
    TestItem("KeyZoom", KeyTestScreen()),
)