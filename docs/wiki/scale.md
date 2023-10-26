## Scale Image/缩放图像

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is
    different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

### Scale Features/缩放特性

* Support [One-Finger Scale](#one-finger-scale单指缩放)
  , Two-Finger Scale, [Double-click Scale](#double-click-scale双击缩放)and scaling to a specified
  multiple by the [scale()](#scale--) method
* [Supports rubber band effect](#rubber-band-scale橡皮筋效果).
  When the gesture is continuously zoomed (one-finger/two-finger scale) exceeds the maximum or
  minimum range, zooming can continue, but there is a damping effect, and it will spring back to the
  maximum or minimum zoom multiplier when released
* [Dynamic scaling range](#minscale-mediumscale-maxscale). Default based on
  containerSize, contentSize, contentOriginSize dynamically calculate mediumScale and maxScale
* [Support for animation](#animation动画). Both the scale() method and double-click scaling support
  animation
* [All ContentScale and Alignment are supported](#contentscale-alignment)，ZoomImageView also
  supports ContentScale and Alignment
* [Support for disabling gestures](#disabled-gestures禁用手势). Supports disabling gestures such as
  double-click scale, two-finger scale, one-finger scale, and drag
* Only when the containerSize changes (dragging to resize the window on the desktop), ZoomImage will
  keep the scale factor and content visible center point unchanged
* When the page is rebuilt (the screen rotates, the app is recycled in the background), the scale
  and offset are reset
* [Open the Modifier.zoom() function](#modifierzoom--), which can be applied to any component
* [Supports reading all scaling-related information](#get-relevant-information读取相关信息)
  <br>-----------</br>
* 支持[单指长按后上下滑动缩放](#one-finger-scale单指缩放)
  、双指捏合缩放、[双击循环缩放](#double-click-scale双击缩放)以及通过 [scale()](#scale--) 方法缩放到指定的倍数
* [支持橡皮筋效果](#rubber-band-scale橡皮筋效果).
  手势连续缩放时（单指/双指缩放）超过最大或最小范围时可以继续缩放，但有阻尼效果，松手后会回弹到最大或最小缩放倍数
* [动态缩放范围](#minscale-mediumscale-maxscale). 默认根据
  containerSize、contentSize、contentOriginSize 动态计算 mediumScale 和 maxScale
* [支持动画](#animation动画). scale() 方法和双击缩放均支持动画
* [支持全部的 ContentScale, 和 Alignment](#contentscale-alignment)，ZoomImageView 也支持 ContentScale
  和 Alignment
* [支持禁用手势](#disabled-gestures禁用手势). 支持分别禁用双击缩放、双指缩放、单指缩放、拖动等手势
* 仅 containerSize 改变时（桌面平台上拖动调整窗口大小），ZoomImage 会保持缩放比例和 content 可见中心点不变
* 页面重建时（屏幕旋转、App 在后台被回收）会重置缩放和偏移
* [开放 Modifier.zoom() 函数](#modifierzoom--)，可以应用在任意组件上
* [支持读取全部的缩放相关信息](#get-relevant-information读取相关信息)

### ContentScale, Alignment

ZoomImage supports all [ContentScale] and [Alignment], and because the compose version and the view
version use the same algorithm, view The version of the component supports [ContentScale]
and [Alignment] in addition to [ScaleType]
<br>-----------</br>
ZoomImage 支持所有的 [ContentScale] 和 [Alignment]，并且得益于 compose 版本和 view 版本使用的是同一套算法，view
版本的组件在支持 [ScaleType] 之外也支持 [ContentScale] 和 [Alignment]

example/示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomable.contentScale = ContentScaleCompat.None
sketchZoomImageView.zoomable.alignment = AlignmentCompat.BottomEnd
```

### minScale, mediumScale, maxScale

The ZoomImage is always controlled by three parameters in the process of scaling: minScale,
mediumScale, and maxScale:
<br>-----------</br>
ZoomImage 在缩放的过程中始终受 minScale、mediumScale、maxScale 三个参数的控制：

* minScale：
    * The minimum zoom multiplier, which limits the minimum value of ZoomImage during scaling, is
      calculated as:
      <br>-----------</br>
    * 最小缩放倍数，用于限制 ZoomImage 在缩放过程中的最小值，计算公式为：
        ```kotlin
        ContentScale.computeScaleFactor(srcSize, dstSize).scaleX
        ```
* mediumScale：
    * The intermediate zoom multiplier is specially used for double-click scaling, and the value is
      controlled by the scalesCalculator parameter
      <br>-----------</br>
    * 中间缩放倍数，专门用于双击缩放，取值受 scalesCalculator 参数控制
* maxScale：
    * The maximum zoom multiplier is used to limit the maximum value of ZoomImage during scaling,
      and the value is controlled by the scalesCalculator parameter
      <br>-----------</br>
    * 最大缩放倍数，用于限制 ZoomImage 在缩放过程中的最大值，取值受 scalesCalculator 参数控制

#### ScalesCalculator

[ScalesCalculator] is designed to calculate mediumScale and maxScale, and ZoomImage has two
built-in [ScalesCalculator]:
<br>-----------</br>
[ScalesCalculator] 专门用来计算 mediumScale 和 maxScale，ZoomImage 有两个内置的 [ScalesCalculator]：

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
          <br>-----------</br>
    * maxScale 始终是 `mediumScale * multiple`，mediumScale 则是根据
      containerSize、contentSize、contentOriginSize 动态的计算，计算规则是在以下几个值中取最大的：
        * minMediumScale：最小中间缩放倍数，计算公式为：
          ```kotlin
          minScale * multiple
          ```
        * fillContainerScale：完全充满容器时的缩放倍数，效果类似 ContentScale.Crop，计算公式为：
          ```kotlin
          max(
              containerSize.width / contentSize.width.toFloat(), 
              containerSize.height / contentSize.height.toFloat()
          )
          ```
        * originScale：将 contentSize 缩放到 contentOriginSize 的倍数，计算公式为：
          ```kotlin
          max(
              contentOriginSize.width / contentSize.width.toFloat(), 
              contentOriginSize.height / contentSize.height.toFloat()
          )
          ```
        * 另外当 initialScale 大于 minScale 并且 initialScale 和 mediumScale 的差值小于 mediumScale
          乘以 differencePercentage 时用将 initialScale 作为 mediumScale。initialScale 通常由
          ReadMode 决定
* [ScalesCalculator].Fixed：
    * maxScale is always `mediumScale * multiple`
    * The mediumScale calculation rule is used if initialScale is greater than minScale
      initialScale, otherwise use 'minScale * multiple'
      <br>-----------</br>
    * maxScale 始终是 `mediumScale * multiple`
    * mediumScale 计算规则是如果 initialScale 大于 minScale 则用
      initialScale，否则用 `minScale * multiple`

> The default value for multiple is 3f
<br>-----------</br>
> multiple 默认值为 3f
> differencePercentage 默认值为 0.3f

scalesCalculator defaults to [ScalesCalculator]. Dynamic, which you can modify into a Fixed or
custom implementation
<br>-----------</br>
scalesCalculator 默认值为 [ScalesCalculator].Dynamic，你可以将它修改为 Fixed 或自定义的实现

example/示例：

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

### Double-click Scale/双击缩放

When you double-click the image, ZoomImage zooms to the next zoom factor, always looping between
minScale and mediumScale by default
<br>-----------</br>
双击图像时 ZoomImage 会缩放到下一个缩放倍数，默认总是在 minScale 和 mediumScale 之间循环

If you want to loop between minScale, mediumScale and maxScale, you can change threeStepScale
property to true
<br>-----------</br>
如果你想在 minScale、mediumScale 和 maxScale 之间循环，可以修改 threeStepScale 属性为 true

example/示例：

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

Double-clicking to zoom invokes ZoomImage's switchScale() method, or you can call switchScale() when
needed
The method toggles the zoom factor, which has two parameters:
<br>-----------</br>
双击缩放调用的是 ZoomImage 的 switchScale() 方法，你也可以在需要的时候调用 switchScale()
方法来切换缩放倍数，它有两个参数：

* centroidContentPoint: IntOffset = contentVisibleRect.center。
    * The zoom center point on Content, the origin is the upper-left corner of Content, and the
      default is the center of Content's currently visible area
      <br>-----------</br>
    * content 上的缩放中心点，原点是 content 的左上角，默认是 content 当前可见区域的中心
* animated: Boolean = false。
    * Whether to use animation, the default is false
      <br>-----------</br>
    * 是否使用动画，默认为 false

> Note: centroidContentPoint must be a point on content
<br>-----------</br>
> 注意：centroidContentPoint 一定要是 content 上的点

example/示例：

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

You can also call the getNextStepScale() method to get the next scale multiplier
<br>-----------</br>
你还可以调用 getNextStepScale() 方法来获取下一个缩放倍数

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.getNextStepScale()
```

### One Finger Scale/单指缩放

ZoomImage supports one-finger scale the image, and after turning on, one finger long press on the
screen triggers the long press behavior and then swipe up and down to scale
the image. This feature is turned off by default and you can pass The oneFingerScaleSpec
property turns it on
<br>-----------</br>
ZoomImage 支持单指缩放图像，开启后单指按住屏幕触发长按后上下滑动即可缩放图像。此功能默认关闭，你可以通过
oneFingerScaleSpec 属性开启它

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turned, but no haptic feedback after the long-press behavior triggers
    // 开启，但长按行为触发后没有触觉反馈
    state.zoomable.oneFingerScaleSpec = OneFingerScaleSpec.Default

    // Turned, and there will be vibration feedback after the long-press behavior is triggered
    // 开启，并且长按行为触发后会有震动反馈
    state.zoomable.oneFingerScaleSpec = OneFingerScaleSpec.vibration(context)

    // Closed
    // 关闭
    state.zoomable.oneFingerScaleSpec = null
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
<br>-----------</br>
ZoomImage 提供了 scale() 方法用来缩放图像到指定的倍数，它有三个参数：

* targetScale: Float。
    * Target scale multiple
      <br>-----------</br>
    * 目标缩放倍数
* centroidContentPoint: IntOffset = contentVisibleRect.center。
    * The scale center point on the content, the origin is the upper-left corner of the content, and
      the default is the center of the currently visible area of the content
      <br>-----------</br>
    * content 上的缩放中心点，原点是 content 的左上角， 默认是 content 当前可见区域的中心
* animated: Boolean = false。
    * Whether to use animation, the default is false
      <br>-----------</br>
    * 是否使用动画，默认为 false

> Note: centroidContentPoint must be a point on content
<br>-----------</br>
> 注意：centroidContentPoint 一定要是 content 上的点

example/示例：

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

### Rubber Band Scale/橡皮筋效果

ZoomImage will limit the scale multiplier between `minScale` and `maxScale`, if it exceeds this
range when scaling with one or two fingers, you can continue to scale, but there will be a damping
effect similar to a rubber band, and it will spring back to `minScale` or `maxScale` after letting
go, this function is turned on by default, you can turn it off through the `rubberBandScale`property
<br>-----------</br>
ZoomImage 会将缩放倍数限制在 `minScale` 和 `maxScale`之间，单指或双指缩放时如果超过了这个范围依然可以继续缩放，
但会有类似橡皮筋的阻尼效果，松手后会回弹到 `minScale`或 `maxScale`
，此功能默认开启，你可通过 `rubberBandScale` 属性关闭它

example/示例：

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

### Animation/动画

ZoomImage provides animationSpec parameters to modify the duration, Ease, and initial speed of the
zoom animation
<br>-----------</br>
ZoomImage 提供了 animationSpec 参数用来修改缩放动画的时长、Easing 以及初始速度

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.animationSpec = ZoomAnimationSpec(
        durationMillis = 500,
        easing = LinearOutSlowInEasing,
        initialVelocity = 10f
    )

    // Or modify some parameters based on the default values
    // 或者在默认值的基础上修改部分参数
    state.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Disabled gestures/禁用手势

ZoomImage supports gestures such as double-click zoom, two-finger zoom, one-finger zoom, drag, etc.,
which are enabled by default, and you can disable them through the disabledGestureType property
<br>-----------</br>
ZoomImage 支持双击缩放、双指缩放、单指缩放、拖动等手势，这些手势除单指缩放外默认都是开启的，你可以通过
disabledGestureType 属性来禁用它们

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turn off all scale gestures and keep only the drag gesture
    // 关闭所有缩放手势，只保留拖动手势
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

The Compose version of the ZoomImage component relies on Modifier.zoom() for scaling, and it can
also be used on any Compose component
<br>-----------</br>
Compose 版本的 ZoomImage 组件依赖 Modifier.zoom() 实现缩放，它还可以用在任意 Compose 组件上

example/示例：

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

### Get relevant information/读取相关信息

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
val zoomable: ZoomableState = state.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> * Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
    suffixed with State compared to the compose version
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.transform.scale: ScaleFactor`。
    * Current scaling (baseTransform.scale * userTransform.scale)
      <br>-----------</br>
    * 当前缩放比例（baseTransform.scale * userTransform.scale）
* `zoomable.baseTransform.scale: ScaleFactor`。
    * The current underlying scale, affected by the contentScale parameter
      <br>-----------</br>
    * 当前基础缩放比例，受 contentScale 参数影响
* `zoomable.userTransform.scale: ScaleFactor`。
    * The current user scaling factor is affected by scale(), locate(), user gesture zoom,
      double-click and other operations
      <br>-----------</br>
    * 当前用户缩放比例，受 scale()、locate() 以及用户手势缩放、双击等操作影响
* `zoomable.minScale: Float`。
    * Minimum scale factor, for limits the final scale factor, and as a target value for one of when
      switch scale
      <br>-----------</br>
    * 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.mediumScale: Float`。
    * Medium scale factor, only as a target value for one of when switch scale
      <br>-----------</br>
    * 中间缩放比例，用于双击缩放时的一个循环缩放比例
* `zoomable.maxScale: Float`。
    * Maximum scale factor, for limits the final scale factor, and as a target value for one of when
      switch scale
      <br>-----------</br>
    * 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例

#### Listen property changed/监听属性变化

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
  <br>-----------</br>
  compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening
  <br>-----------</br>
  view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScalesCalculator]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScalesCalculator.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ScaleType]: https://developer.android.com/reference/android/widget/ImageView.ScaleType