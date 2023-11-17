## Scale

Translations: [简体中文](scale_zh.md)

> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

### Features

* Support [One-Finger Scale](#one-finger-scale)
  , Two-Finger Scale, [Double-click Scale](#double-click-scale)and scaling to a specified
  multiple by the [scale()](#scale) method
* [Supports rubber band effect](#rubber-band-scale).
  When the gesture is continuously zoomed (one-finger/two-finger scale) exceeds the maximum or
  minimum range, zooming can continue, but there is a damping effect, and it will spring back to the
  maximum or minimum zoom multiplier when released
* [Dynamic scaling range](#minscale-mediumscale-maxscale). Default based on
  containerSize, contentSize, contentOriginSize dynamically calculate mediumScale and maxScale
* [Support for animation](#animation). Both the scale() method and double-click scaling support
  animation
* [All ContentScale and Alignment are supported](#contentscale-alignment)，ZoomImageView also
  supports ContentScale and Alignment
* [Support for disabling gestures](#disabled-gestures). Supports disabling gestures such as
  double-click scale, two-finger scale, one-finger scale, and drag
* Only when the containerSize changes (dragging to resize the window on the desktop), ZoomImage will
  keep the scale factor and content visible center point unchanged
* When the page is rebuilt (the screen rotates, the app is recycled in the background), the scale
  and offset are reset
* [Open the Modifier.zoom() function](#modifierzoom), which can be applied to any component
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

sketchZoomImageView.zoomable.contentScale = ContentScaleCompat.None
sketchZoomImageView.zoomable.alignment = AlignmentCompat.BottomEnd
```

### minScale, mediumScale, maxScale

The ZoomImage is always controlled by three parameters in the process of scaling: minScale,
mediumScale, and maxScale:

* `minScale`：The minimum zoom multiplier, which limits the minimum value of ZoomImage during
  scaling,
  is calculated as:
    ```kotlin
    ContentScale.computeScaleFactor(srcSize, dstSize).scaleX
    ```
* `mediumScale`：The intermediate zoom multiplier is specially used for double-click scaling, and the
  value is controlled by the scalesCalculator parameter
* `maxScale`：The maximum zoom multiplier is used to limit the maximum value of ZoomImage during
  scaling, and the value is controlled by the scalesCalculator parameter

#### ScalesCalculator

[ScalesCalculator] is specially used to calculate mediumScale and maxScale. ZoomImage has two
built-in [ScalesCalculator]:

* [ScalesCalculator].Dynamic：
    * maxScale is always `mediumScale * multiple`, mediumScale is dynamically calculated according
      to containerSize, contentSize, contentOriginSize, and the calculation rule is the largest of
      the following values:
        * minMediumScale：The minimum intermediate zoom factor, calculated as:
          ```kotlin
          minScale * multiple
          ```
        * fillContainerScale：The zoom multiplier when the container is completely full, similar to
          ContentScale.Crop, is calculated as:
          ```kotlin
          max(
              containerSize.width / contentSize.width.toFloat(), 
              containerSize.height / contentSize.height.toFloat()
          )
          ```
        * originScale：Scale contentSize to a multiple of contentOriginSize, calculated as:
          ```kotlin
          max(
              contentOriginSize.width / contentSize.width.toFloat(), 
              contentOriginSize.height / contentSize.height.toFloat()
          )
          ```
        * In addition, when initialScale is greater than minScale and the difference between
          initialScale and mediumScale is less than mediumScale multiplied by differencePercentage,
          initialScale is used as mediumScale. initialScale is usually determined by ReadMode
* [ScalesCalculator].Fixed：
    * maxScale is always `mediumScale * multiple`
    * The mediumScale calculation rule is used if initialScale is greater than minScale
      initialScale, otherwise use 'minScale * multiple'

> The default value for multiple is 3f, differencePercentage is 0.3f

scalesCalculator defaults to [ScalesCalculator]. Dynamic, which you can modify into a Fixed or
custom implementation

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.zoomable.scalesCalculator = ScalesCalculator.Fixed
    // or
    state.zoomable.scalesCalculator = MyScalesCalculator()
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Double-click Scale

When you double-click the image, ZoomImage zooms to the next zoom factor, always looping between
minScale and mediumScale by default

If you want to loop between minScale, mediumScale and maxScale, you can change threeStepScale
property to true

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.zoomable.threeStepScale = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

Double-clicking to zoom invokes ZoomImage's `switchScale()` method, or you can call `switchScale()`
when
needed The method toggles the zoom factor, which has two parameters:

* `centroidContentPoint: IntOffset = contentVisibleRect.center`: The zoom center point on Content,
  the origin is the upper-left corner of Content, and the default is the center of Content's
  currently visible area
* `animated: Boolean = false`: Whether to use animation, the default is false

> Note: centroidContentPoint must be a point on content

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
            state.zoomable.switchScale(animated = true)
        }
    }
) {
    Text(text = "switch scale")
}
```

You can also call the `getNextStepScale()` method to get the next scale multiplier

example：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.getNextStepScale()
```

### One Finger Scale

ZoomImage supports scaling images with one finger. Double-click and hold the screen and slide up or
down to zoom the image. This feature is enabled by default, you can turn it off
by [Disabled gestures](#disabled-gestures)

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turn off one-finger scale gesture
    state.zoomable.disabledGestureType = GestureType.ONE_FINGER_SCALE
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
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

> Note: centroidContentPoint must be a point on content

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
            val targetScale = state.zoomable.transform.scaleX + 0.2f
            state.zoomable.scale(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "scale + 0.2")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetScale = state.zoomable.transform.scaleX - 0.2f
            state.zoomable.scale(targetScale = targetScale, animated = true)
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
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.zoomable.rubberBandScale = false
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Animation

ZoomImage provides `animationSpec` parameters to modify the duration, Ease, and initial speed of the
zoom animation

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.animationSpec = ZoomAnimationSpec(
        durationMillis = 500,
        easing = LinearOutSlowInEasing,
        initialVelocity = 10f
    )

    // Or modify some parameters based on the default values
    state.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Disabled gestures

ZoomImage supports gestures such as double-click zoom, two-finger zoom, one-finger zoom, drag, etc.,
which are enabled by default, and you can disable them through the `disabledGestureType` property

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turn off all scale gestures and keep only the drag gesture
    state.zoomable.disabledGestureType =
        GestureType.TWO_FINGER_SCALE or GestureType.ONE_FINGER_SCALE or GestureType.DOUBLE_TAP_SCALE
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Modifier.zoom()

The Compose version of the ZoomImage component relies on `Modifier.zoom()` for scaling, and it can
also be used on any Compose component

example：

```kotlin
val logger = rememberZoomImageLogger()
val zoomState = rememberZoomableState(logger)
val text = remember {
    """
    六王毕，四海一，蜀山兀，阿房出。覆压三百余里，隔离天日。骊山北构而西折，直走咸阳。二川溶溶，流入宫墙。五步一楼，十步一阁；廊腰缦回，檐牙高啄；各抱地势，钩心斗角。盘盘焉，囷囷焉，蜂房水涡，矗不知其几千万落。长桥卧波，未云何龙？复道行空，不霁何虹？高低冥迷，不知西东。歌台暖响，春光融融；舞殿冷袖，风雨凄凄。一日之内，一宫之间，而气候不齐。　　

    妃嫔媵嫱，王子皇孙，辞楼下殿，辇来于秦。朝歌夜弦，为秦宫人。明星荧荧，开妆镜也；绿云扰扰，梳晓鬟也；渭流涨腻，弃脂水也；烟斜雾横，焚椒兰也。雷霆乍惊，宫车过也；辘辘远听，杳不知其所之也。一肌一容，尽态极妍，缦立远视，而望幸焉。有不见者三十六年。燕赵之收藏，韩魏之经营，齐楚之精英，几世几年，剽掠其人，倚叠如山。一旦不能有，输来其间。鼎铛玉石，金块珠砾，弃掷逦迤，秦人视之，亦不甚惜。
　  
    嗟乎！一人之心，千万人之心也。秦爱纷奢，人亦念其家。奈何取之尽锱铢，用之如泥沙？使负栋之柱，多于南亩之农夫；架梁之椽，多于机上之工女；钉头磷磷，多于在庾之粟粒；瓦缝参差，多于周身之帛缕；直栏横槛，多于九土之城郭；管弦呕哑，多于市人之言语。使天下之人，不敢言而敢怒。独夫之心，日益骄固。戍卒叫，函谷举，楚人一炬，可怜焦土！　　
    
    呜呼！灭六国者六国也，非秦也；族秦者秦也，非天下也。嗟乎！使六国各爱其人，则足以拒秦；使秦复爱六国之人，则递三世可至万世而为君，谁得而族灭也？秦人不暇自哀，而后人哀之；后人哀之而不鉴之，亦使后人而复哀后人也。

                                ——唐代·杜牧《阿房宫赋》
""".trimIndent()
}
Box(
    modifier = Modifier
        .fillMaxSize()
        .zoom(logger, zoomState)
) {
    Text(
        text = text,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp)
    )
}
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

* `zoomable.transform.scale: ScaleFactor`: Current scaling (baseTransform.scale *
  userTransform.scale)
* `zoomable.baseTransform.scale: ScaleFactor`: The current underlying scale, affected by the
  contentScale parameter
* `zoomable.userTransform.scale: ScaleFactor`: The current user scaling factor is affected by
  scale(), locate(), user gesture zoom, double-click and other operations
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

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScalesCalculator]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScalesCalculator.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ScaleType]: https://developer.android.com/reference/android/widget/ImageView.ScaleType