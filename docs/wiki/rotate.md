## Rotate

Translations: [简体中文](rotate_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

### rotate()

ZoomImage provides a modified `rotate()` method to rotate the image to a specified angle, which has
one parameter:

* `targetRotation: Int`: Target rotation angle, which can only be a multiple of 90, such as 0, 90,
  180, 270, 360, etc

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
  imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)

val coroutineScope = rememberCoroutineScope()
Button(
    onClick = {
        coroutineScope.launch {
            val targetRotation = zoomState.zoomable.transform.rotation.roundToInt() + 90
            zoomState.zoomable.rotate(targetRotation = targetRotation)
        }
    }
) {
    Text(text = "right rotate 90")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetRotation = zoomState.zoomable.transform.rotation.roundToInt() - 90
            zoomState.zoomable.rotate(targetRotation = targetRotation)
        }
    }
) {
    Text(text = "left rotate 90")
}
```

### Public Properties

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val zoomable: ZoomableState = zoomState.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
> suffixed with State compared to the compose version

* `zoomable.transform.rotation: Float`: Current rotation angle (base rotation angle + user rotation
  angle)
* `zoomable.baseTransform.rotation: Float`: The current base rotation angle, affected by the
  rotate() method
* `zoomable.userTransform.rotation: Float`: The current user rotation angle, which is always 0

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt