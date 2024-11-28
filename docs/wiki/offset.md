## Offset

Translations: [简体中文](offset_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage supports one-finger drag, inertial swipe, keyboard drag, and the `offset()` method to
move the image.

### One Finger Drag

ZoomImage enables one finger drag gestures by default, but you can turn it off as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.ONE_FINGER_DRAG
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Keyboard drag

ZoomImage supports drag images through the keyboard, supports both short press and long press
operations. And the following keys are registered by default:

* move up: Key.DirectionUp
* move down: Key.DirectionDown
* move left: Key.DirectionLeft
* move right: Key.DirectionRight

Since the keyboard drag function must rely on focus, and focus management is very complex, it is not
enabled by default. You need to actively configure and request focus, as follows:

```kotlin
val focusRequester = remember { FocusRequester() }
val zoomState = rememberSketchZoomState()
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    zoomState = zoomState,
    modifier = Modifier.fillMaxSize()
        .focusRequester(focusRequester)
        .focusable()
        .keyZoom(zoomState.zoomable),
)
LaunchedEffect(Unit) {
    focusRequester.requestFocus()
}
```

> [!TIP]
> When requesting focus in HorizontalPager, you need to note that you can only request focus for the
> current page, otherwise it will cause unexpected accidents.

You can also turn it off dynamically via gesture control, as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.KEYBOARD_DRAG
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### offset()

ZoomImage provides a modified `offset()` method to move the image to a specified position, which has
two parameters:

* `targetOffset: Offset`: The target offset, with the offset origin being the upper-left corner of
  the component
* `animated: Boolean = false`: Whether to use animation, the default is false

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
            val targetOffset = zoomState.zoomable.transform.offset + Offset(x = 100, y = 200)
            zoomState.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = zoomState.zoomable.transform.offset - Offset(x = 100, y = 200)
            zoomState.zoomable.offset(targetOffset = targetOffset, animated = true)
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
`limitOffsetWithinBaseVisibleRect parameter` to true to achieve this

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zommable.limitOffsetWithinBaseVisibleRect = true
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
  modifier = Modifier.fillMaxSize(),
  zoomState = zoomState,
)
```

### Container Whitespace

By default, ZoomImage always aligns the edge of the image with the edge of the container when
dragging the image, and there will be no white space between them (except in the initial state of
the image). When you need to leave a white space between the image and the container, you can pass
To achieve this, set the `containerWhitespace` or `containerWhitespaceMultiple` parameter to

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
  // Set the specific size through the containerWhitespace property
  zoomState.zommable.containerWhitespace = ContainerWhitespace(
    left = 4f, top = 3f, right = 2f, bottom = 1f
  )
  // or
  zoomState.zommable.containerWhitespace = ContainerWhitespace(horizontal = 2f, vertical = 1f)
  // or
  zoomState.zommable.containerWhitespace = ContainerWhitespace(size = 1f)

  // Leave 50% of the container size white space between the edge of the image and the edge of the container
  zoomState.zommable.containerWhitespaceMultiple = 0.5f
}

SketchZoomAsyncImage(
  imageUri = "https://sample.com/sample.jpeg",
  contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
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

> [!TIP]
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

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScrollEdge]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScrollEdge.kt