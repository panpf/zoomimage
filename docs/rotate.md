## Rotate

Translations: [简体中文](rotate.zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

### rotate()

ZoomImage provides a modified `rotate()` method to rotate the image to a specified angle, which has
one parameter:

* `targetRotation: Int`: Target rotation angle, which can only be a multiple of 90, such as 0, 90,
  180, 270, 360, etc

Example:

```kotlin
val zoomState: ZoomState by rememberSketchZoomState()

SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
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
val zoomState: ZoomState by rememberSketchZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val zoomable: ZoomableState = zoomState.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
> suffixed with State compared to the compose version

Readable properties:

* `zoomable.transform.rotation: Float`: Current rotation angle (base rotation angle + user rotation
  angle)
* `zoomable.baseTransform.rotation: Float`: The current base rotation angle, affected by the
  rotate() method
* `zoomable.userTransform.rotation: Float`: The current user rotation angle, which is always 0
* `zoomable.contentBaseDisplayRectF: Rect`: The content region in the container after the
  baseTransform transformation
* `zoomable.contentBaseDisplayRect: IntRect`: The content region in the container after the
  baseTransform transformation
* `zoomable.contentBaseVisibleRectF: Rect`: The content is visible region to the user after the
  baseTransform transformation
* `zoomable.contentBaseVisibleRect: IntRect`: The content is visible region to the user after the
  baseTransform transformation
* `zoomable.contentDisplayRectF: Rect`: The content region in the container after the final
  transform transformation
* `zoomable.contentDisplayRect: IntRect`: The content region in the container after the final
  transform transformation
* `zoomable.contentVisibleRectF: Rect`: The content is visible region to the user after the final
  transform transformation
* `zoomable.contentVisibleRect: IntRect`: The content is visible region to the user after the final
  transform transformation
* `zoomable.sourceVisibleRectF: Rect`: contentVisibleRect maps to the area on the original image
* `zoomable.sourceVisibleRect: IntRect`: contentVisibleRect maps to the area on the original image

Interactive methods:

* `zoomable.rotate()`: Rotate content to the specified angle, the angle can only be multiples of 90
* `zoomable.rotateBy()`: Rotate the angle specified by content in incremental manner, the angle can
  only be multiples of 90.
* `zoomable.touchPointToContentPoint(): IntOffset`: Convert the touch point to a point on the
  content, the origin is the upper left corner of the content
* `zoomable.touchPointToContentPointF(): Offset`: Convert the touch point to a point on the content,
  the origin is the upper left corner of the content
* `zoomable.sourceToDraw(Offset): Offset`: Convert the points on the original image to the points at
  the time of drawing, the origin is the upper left corner of the container
* `zoomable.sourceToDraw(Rect): Rect`: Convert the rectangle on the original image to the rectangle
  when drawing, the origin is the upper left corner of the container

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

[ZoomImageView]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt