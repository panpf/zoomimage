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

ZoomImage supports multiple zoom methods such as double-click zoom, gesture zoom, scale() and other
zoom methods to scale the image.
<br>-----------</br>
ZoomImage 支持双击缩放、双指捏合缩放、单指长按滑动缩放、scale() 等多种方式来缩放图像，在连续缩放时还支持阻尼和动画效果。

### minScale, mediumScale, maxScale

The ZoomImage is always controlled by three parameters in the process of scaling: minScale,
mediumScale, and maxScale:
<br>-----------</br>
ZoomImage 在缩放的过程中始终受到 minScale、mediumScale、maxScale 三个参数控制：

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
scalesCalculator 默认值为 [ScalesCalculator].Dynamic，你可以将它修改 Fixed 或自定义的实现

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.scalesCalculator = ScalesCalculator.Fixed
// or
val myScalesCalculator = remember { MyScalesCalculator() }
state.zoomable.scalesCalculator = myScalesCalculator

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

If you want to loop between minScale, mediumScale and maxScale, you can change threeStepScale to
true
<br>-----------</br>
如果你想在 minScale、mediumScale 和 maxScale 之间循环，可以修改 threeStepScale 为 true

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.threeStepScale = true

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

### Long Press Slide Scale/单指长按滑动缩放

ZoomImage supports one-finger long press up and down to zoom the image, and after turning on, one
finger long press on the screen triggers the long press behavior and then swipe up and down to zoom
the image. This feature is turned off by default and you can pass The longPressSlideScaleSpec
property turns it on
<br>-----------</br>
ZoomImage 支持单指长按上下滑动缩放图像，开启后单指按住屏幕触发长按后上下滑动即可缩放图像。此功能默认关闭，你可以通过
longPressSlideScaleSpec 属性开启它

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

// Turned, but no haptic feedback after the long-press behavior triggers
// 开启，但长按行为触发后没有触觉反馈
state.zoomable.longPressSlideScaleSpec = LongPressSlideScaleSpec.Default

// Turned, and there will be vibration feedback after the long-press behavior is triggered
// 开启，并且长按行为触发后会有震动反馈
state.zoomable.longPressSlideScaleSpec = LongPressSlideScaleSpec.Vibration

// Closed
// 关闭
state.zoomable.longPressSlideScaleSpec = null

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

### Scale damping/缩放阻尼

ZoomImage will limit the scale factor between minScale and maxScale, and if it exceeds this
range, it will have a rubber band-like damping effect, which will spring back when released
minScale or maxScale, this feature is enabled by default, and you can turn it off via the
rubberBandScale property
<br>-----------</br>
ZoomImage 会将缩放倍数限制在 minScale 和 maxScale 之间，如果超过了这个范围就会有类似橡皮筋的阻尼效果，松手后会回弹到
minScale 或 maxScale，此功能默认开启，你可通过 rubberBandScale 属性关闭它

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.rubberBandScale = false

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Zoom Animation/缩放动画

ZoomImage provides animationSpec parameters to modify the duration, Ease, and initial speed of the
zoom animation
<br>-----------</br>
ZoomImage 提供了 animationSpec 参数用来修改缩放动画的时长、Easing 以及初始速度

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.animationSpec = ZoomAnimationSpec(
    durationMillis = 500,
    easing = LinearOutSlowInEasing,
    initialVelocity = 10f
)

// Or modify some parameters based on the default values
// 或者在默认值的基础上修改部分参数
state.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Get relevant information/获取相关信息

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

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScalesCalculator]: ../../zoomimage-core/src/main/java/com/github/panpf/zoomimage/zoom/ScalesCalculator.kt