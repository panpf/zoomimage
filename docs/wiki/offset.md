## Offset

Translations: [简体中文](offset_zh.md)

> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage supports one-finger drag, two-finger drag, inertial swipe, and the `offset()` method to
move
the image.

### offset()

ZoomImage provides a modified `offset()` method to move the image to a specified position, which has
two parameters:

* `targetOffset: Offset`: The target offset, with the offset origin being the upper-left corner of
  the component
* `animated: Boolean = false`: Whether to use animation, the default is false

example：

```kotlin
val state: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)

val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = state.zoomable.transform.offset + Offset(x = 100, y = 200)
            state.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = state.zoomable.transform.offset - Offset(x = 100, y = 200)
            state.zoomable.offset(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "offset - Offset(100, 200)")
}
```

### Limit the bounds of offset

By default, zoomImage can drag to view the entire content of the image regardless of what you
set [ContentScale], for example, if you set [ContentScale] to Crop and [Alignment] to Center, then
only the middle part of the image is displayed by default, and then you can also drag with one or
two fingers to view the entire content of the image

If you want the image to be moved only within the area restricted
by [ContentScale] and [Alignment], and not the entire content, you can modify the
limitOffsetWithinBaseVisibleRect parameter to true to achieve this

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.limitOffsetWithinBaseVisibleRect = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Public Properties

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(state = state)
val zoomable: ZoomableState = state.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
> suffixed with State compared to the compose version

* `zoomable.transform.offset: Offset`: Current offset (baseTransform.offset + userTransform.offset)
* `zoomable.baseTransform.offset: Offset`: The current base offset, affected by the alignment
  parameter and the rotate method
* `zoomable.userTransform.offset: Offset`: The current user offset, affected by offset(), locate(),
  and user gesture dragging
* `zoomable.scrollEdge: ScrollEdge`: Edge state for the current offset

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScrollEdge]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScrollEdge.kt