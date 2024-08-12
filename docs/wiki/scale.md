## Scale

Translations: [简体中文](scale_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage supports multiple ways to scale images, such as two-finger scale, single-finger scale,
double-click scale, mouse wheel scale, keyboard scale, scale(), etc.

### Features

* Support [One-Finger Scale](#one-finger-scale), [Two-Finger Scale](#two-finger-scale),
  [Double-click Scale](#double-click-scale), [Mouse Wheel Scale](#mouse-wheel-scale), [Keyboard Scale](#keyboard-scale)
  and scaling to a specified multiple by the [scale()](#scale) method
* [Supports rubber band effect](#rubber-band-scale).
  When the gesture is continuously zoomed (one-finger/two-finger scale) exceeds the maximum or
  minimum range, zooming can continue, but there is a damping effect, and it will spring back to the
  maximum or minimum scale multiplier when released
* [Dynamic scaling range](#minscale-mediumscale-maxscale). Default based on
  containerSize, contentSize, contentOriginSize dynamically calculate mediumScale and maxScale
* [Support for animation](#animation). Both the scale() method and double-click scaling support
  animation
* [All ContentScale and Alignment are supported](#contentscale-alignment)，ZoomImageView also
  supports ContentScale and Alignment
* Disabling gestures. Supports disabling gestures such as
  double-click scale, two-finger scale, one-finger scale, mouse wheel scale, and drag
* Only when the containerSize changes (dragging to resize the window on the desktop), ZoomImage will
  keep the scale factor and content visible center point unchanged
* When the page is rebuilt (the screen rotates, the app is recycled in the background), the scale
  and offset are reset
* [Supports reading related information](#public-properties). You can read
  scale-related information such as the current scale multiplier and the minimum, middle, and
  maximum scale multiples

### ContentScale, Alignment

ZoomImage supports all [ContentScale] and [Alignment], and because the compose version and the view
version use the same algorithm, view The version of the component supports [ContentScale]
and [Alignment] in addition to [ScaleType]

example：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomable.contentScaleState.value = ContentScaleCompat.None
sketchZoomImageView.zoomable.alignmentState.value = AlignmentCompat.BottomEnd
```

### minScale, mediumScale, maxScale

The ZoomImage is always controlled by three parameters in the process of scaling: minScale,
mediumScale, and maxScale:

* `minScale`：The minimum scale multiplier, which limits the minimum value of ZoomImage during
  scaling,
  is calculated as:
    ```kotlin
    ContentScale.computeScaleFactor(srcSize, dstSize).scaleX
    ```
* `mediumScale`：The intermediate scale multiplier is specially used for double-click scaling, and
  the
  value is controlled by the scalesCalculator parameter
* `maxScale`：The maximum scale multiplier is used to limit the maximum value of ZoomImage during
  scaling, and the value is controlled by the scalesCalculator parameter

#### ScalesCalculator

[ScalesCalculator] is specially used to calculate mediumScale and maxScale. ZoomImage has two
built-in [ScalesCalculator]:

> [!TIP]
> * minMediumScale = `minScale * multiple`
> * fillContainerScale = `max(containerSize.width / contentSize.width.toFloat(),
    containerSize.height / contentSize.height.toFloat())`
> * contentOriginScale = `max(contentOriginSize.width / contentSize.width.toFloat(),
    contentOriginSize.height / contentSize.height.toFloat())`
> * initialScale usually calculated by ReadMode
> * multiple default value is 3f

* [ScalesCalculator].Dynamic：
    * mediumScale calculation rules are as follows:
        * If contentScale is FillBounds, it is always minMediumScale
        * Always initialScale if initialScale is greater than minScale
        * Otherwise, take the largest among minMediumScale, fillContainerScale, and
          contentOriginScale.
    * maxScale calculation rules are as follows:
        * If contentScale is FillBounds, it is always `mediumScale * multiple`
        * Otherwise, take the largest among `mediumScale * multiple`, contentOriginScale
* [ScalesCalculator].Fixed：
    * mediumScale calculation rules are as follows:
        * If contentScale is FillBounds, it is always minMediumScale
        * Always initialScale if initialScale is greater than minScale
        * Otherwise always minMediumScale
    * maxScale is always `mediumScale * multiple`

scalesCalculator defaults to [ScalesCalculator]. Dynamic, which you can modify into a Fixed or
custom implementation

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zoomable.scalesCalculator = ScalesCalculator.Fixed
    // or
    zoomState.zoomable.scalesCalculator = MyScalesCalculator()
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Two-Finger Scale

You can pinch the scale image with two fingers, and ZoomImage will calculate the scale factor based
on the distance between the two fingers. The pinch-to-scale feature is on by default, but you can
turn it off as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.TWO_FINGER_SCALE
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Double-click Scale

ZoomImage supports double-clicking the image to switch the scale factor

#### threeStepScale

By default, it always cycles between minScale and mediumScale. If you want to cycle between
minScale, mediumScale and maxScale, you can modify it.
The threeStepScale property is true, as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zoomable.threeStepScale = true
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

#### switchScale()

Double-clicking to scale invokes ZoomImage's `switchScale()` method, or you can call `switchScale()`
when
needed The method toggles the scale factor, which has two parameters:

* `centroidContentPoint: IntOffset = contentVisibleRect.center`: The scale center point on Content,
  the origin is the upper-left corner of Content, and the default is the center of Content's
  currently visible area
* `animated: Boolean = false`: Whether to use animation, the default is false

> [!TIP]
> Note: centroidContentPoint must be a point on content

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
            zoomState.zoomable.switchScale(animated = true)
        }
    }
) {
    Text(text = "switch scale")
}
```

#### getNextStepScale()

You can also call the `getNextStepScale()` method to get the next scale multiplier

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

zoomState.zoomable.getNextStepScale()
```

#### Turn off double-click scale

The double-click scale feature is on by default, but you can turn it off as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.DOUBLE_TAP_SCALE
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### One Finger Scale

ZoomImage supports zooming images with one finger. Double-click and hold the screen and slide up and
down to scale the image. This feature is enabled by default, but you can turn it off as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.ONE_FINGER_SCALE
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Mouse Wheel Scale

ZoomImage supports scaling images through the mouse wheel. ZoomImage takes the current mouse
position as the scale center and calculates the scale factor based on the rolling direction and
distance of the mouse wheel.

You can reverse mouse wheel scaling by setting the `reverseMouseWheelScale` property, as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.reverseMouseWheelScale = true
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

The mouse wheel scale function is enabled by default, but you can turn it off as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.MOUSE_WHEEL_SCALE
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Keyboard scale

ZoomImage supports scaling images through the keyboard, supports both short press and long press
operations. And the following keys are registered by default:

* scale in: Key.ZoomIn, Key.Equals + (meta/ctrl)/alt, Key.DirectionUp + (meta/ctrl)/alt
* scale out: Key.ZoomOut, Key.Minus + (meta/ctrl)/alt, Key.DirectionDown + (meta/ctrl)/alt

Since the keyboard zoom function must rely on focus, and focus management is very complex, it is not
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
        zoomState.zoomable.disabledGestureTypes or GestureType.KEYBOARD_SCALE
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### scale()

ZoomImage provides the scale() method to scale the image to a specified multiple, which has three
parameters:

* `targetScale: Float`: Target scale multiple
* `centroidContentPoint: IntOffset = contentVisibleRect.center`: The scale center point on the
  content, the origin is the upper-left corner of the content, and
  the default is the center of the currently visible area of the content
* `animated: Boolean = false`: Whether to use animation, the default is false

> [!TIP]
> Note: centroidContentPoint must be a point on content

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
            val targetScale = zoomState.zoomable.transform.scaleX + 0.2f
            zoomState.zoomable.scale(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "scale + 0.2")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetScale = zoomState.zoomable.transform.scaleX - 0.2f
            zoomState.zoomable.scale(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "scale - 0.2")
}
```

### Rubber Band Scale

ZoomImage 会将缩放倍数限制在 `minScale` 和 `maxScale`之间，单指或双指缩放时如果超过了这个范围依然可以继续缩放，
但会有类似橡皮筋的阻尼效果，松手后会回弹到 `minScale`或 `maxScale`
，此功能默认开启，你可通过 `rubberBandScale` 属性关闭它

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zoomable.rubberBandScale = false
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Animation

ZoomImage provides `animationSpec` parameters to modify the duration, Ease, and initial speed of the
scale animation

example：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.animationSpec = ZoomAnimationSpec(
        durationMillis = 500,
        easing = LinearOutSlowInEasing,
        initialVelocity = 10f
    )

    // Or modify some parameters based on the default values
    zoomState.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)
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

* `zoomable.transform.scale: ScaleFactor`: Current scaling (baseTransform.scale *
  userTransform.scale)
* `zoomable.baseTransform.scale: ScaleFactor`: The current underlying scale, affected by the
  contentScale parameter
* `zoomable.userTransform.scale: ScaleFactor`: The current user scaling factor is affected by
  scale(), locate(), user gesture scale, double-click and other operations
* `zoomable.minScale: Float`: Minimum scale factor, for limits the final scale factor, and as a
  target value for one of when switch scale
* `zoomable.mediumScale: Float`: Medium scale factor, only as a target value for one of when switch
  scale
* `zoomable.maxScale: Float`: Maximum scale factor, for limits the final scale factor, and as a
  target value for one of when switch scale

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScalesCalculator]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScalesCalculator.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ScaleType]: https://developer.android.com/reference/android/widget/ImageView.ScaleType