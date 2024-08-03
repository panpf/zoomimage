## Read Mode

Translations: [简体中文](readmode_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

For long text images, their height is usually very large, if the initial state shows the full image,
then the text content in the image is not clear, the user must double-click to zoom in and slide to
the start position to start reading

For such images, ZoomImage provides a reading mode to fill the screen at the initial state of the
image and locate it to the beginning position, similar to [ContentScale]
.Crop plus [Alignment].TopStart, so that users can directly start reading the
content of the long text image

### Enabled Read Mode

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zoomable.readMode = ReadMode.Default
}

SketchZoomAsyncImage(
  imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### ReadMode

The [ReadMode] class is used to control the read mode, and it has two parameters:

* `sizeType: Int = ReadMode.SIZE_TYPE_HORIZONTAL or ReadMode.SIZE_TYPE_VERTICAL`: The reading mode
  can be used to control which size type of image, the default is both
  horizontal and vertical charts, and the value is  `ReadMode.SIZE_TYPE_HORIZONTA`
  or `ReadMode.SIZE_TYPE_VERTICAL`
* `decider: ReadMode.Decider = ReadMode.Decider.Default`: decider determines whether read mode can
  be used based on contentSize and containerSize, and the default implementation is
  ReadMode.LongImageDecider, which uses read mode only for long images

> [!TIP]
> * The default configuration for ReadMode is ReadMode.Default
> * You can implement the ReadMode.Decider interface to implement your own decision rules

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ReadMode]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ReadMode.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment