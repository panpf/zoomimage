## Scale Image/缩放图像

> * The following example takes precedence over the Compose version of the ZoomImage component for demonstration
> * The API of ZoomImageView is exactly the same as ZoomImage, except that the entrance is different
> * ZoomState.zoomable is equivalent to ZoomImageView.zoomAbility
> * ZoomState.subsampling is equivalent to ZoomImageView.subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的 ZoomImage 组件来演示
> * ZoomImageView 的 API 和 ZoomImage 一模一样，只是入口不一样
> * ZoomState.zoomable 等价于 ZoomImageView.zoomAbility
> * ZoomState.subsampling 等价于 ZoomImageView.subsamplingAbility

zoomimage 支持双击缩放，手势缩放，scale() 等多种缩放方式来缩放图像。

### minScale, mediumScale, maxScale

zoomimage 在缩放的过程中始终受到 minScale、mediumScale、maxScale 三个参数控制：

* minScale：最小缩放倍数，用于限制 zoomimage 在缩放过程中的最小值，计算公式为：
    ```kotlin
    ContentScale.computeScaleFactor(srcSize, dstSize).scaleX
    ```
* mediumScale：中间缩放倍数，专门用于双击缩放，取值受 scalesCalculator 参数控制
* maxScale：最大缩放倍数，用于限制 zoomimage 在缩放过程中的最大值，取值受 scalesCalculator 参数控制

#### ScalesCalculator

ScalesCalculator 专门用来计算 mediumScale 和 maxScale，zoomimage 有两个内置的 ScalesCalculator：

* ScalesCalculator.Dynamic：maxScale 始终是 `mediumScale * multiple`，mediumScale 则是根据
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
* ScalesCalculator.Fixed：固定的缩放倍数，mediumScale 始终是 `minScale * multiple`，maxScale
  始终是 `mediumScale * multiple`

> multiple 默认值为 3f

scalesCalculator 默认值为 ScalesCalculator.Dynamic，你可以将它修改 Fixed，或自定义的实现，如下：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.scalesCalculator = ScalesCalculator.Fixed
// or
val myScalesCalculator by remember { mutableStateof(MyScalesCalculator()) }
state.zoomable.scalesCalculator = myScalesCalculator

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### 双击缩放

双击图像时 zoomimage 会缩放到下一个缩放倍数，默认总是在 minScale 和 mediumScale 之间循环

如果你想在 minScale、mediumScale 和 maxScale 之间循环，可以修改 threeStepScale 为 true，如下：

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

双击缩放调用的是 zoomimage 的 switchScale() 方法，你也可以在需要的时候调用 switchScale()
方法来切换缩放倍数，它有两个参数：

* centroidContentPoint: IntOffset = contentVisibleRect.center。content 上的缩放中心点，原点是 content
  的左上角，默认是 content 当前可见区域的中心
* animated: Boolean = false。是否使用动画，默认为 false

> 注意：centroidContentPoint 一定要是 content 上的点

示例：

```kotlin
val state: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)

Button(
    onClick = {
        state.zoomable.switchScale(animated = true)
    }
) {
    Text(text = "switch scale")
}
```

你还可以调用 getNextStepScale() 方法来获取下一个缩放倍数，如下：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable.getNextStepScale()
```

### 手势缩放

双指捏合或撑开 zoomimage 会缩小或放大图像，并且会自动限制缩放倍数在 minScale 和 maxScale 之间

在手势缩放时如果超过了 minScale 或 maxScale，zoomimage 会有类似橡皮筋的阻尼效果，松手后会回弹到
minScale 或 maxScale，如果不需要这个效果，可以将 rubberBandScale 设置为 false，如下：

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

### scale()

zoomimage 提供了 scale() 方法用来缩放图像到指定的倍数，它有三个参数：

* targetScale: Float。目标缩放倍数
* centroidContentPoint: IntOffset = contentVisibleRect.center。content 上的缩放中心点，原点是 content
  的左上角， 默认是 content 当前可见区域的中心
* animated: Boolean = false。是否使用动画，默认为 false

> 注意：centroidContentPoint 一定要是 content 上的点

示例：

```kotlin
val state: ZoomState by rememberZoomState()

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)

Button(
    onClick = {
        val targetScale = state.zoomable.transform.scaleX + 0.2f
        state.zoomable.scale(targetScale = targetScale, animated = true)
    }
) {
    Text(text = "scale + 0.2")
}

Button(
    onClick = {
        val targetScale = state.zoomable.transform.scaleX - 0.2f
        state.zoomable.scale(targetScale = targetScale, animated = true)
    }
) {
    Text(text = "scale - 0.2")
}
```

### 缩放动画

zoomimage 提供了 animationSpec 参数来修改缩放动画的时长、Easing 以及初始速度，如下：

```kotlin
val state: ZoomState by rememberZoomState()

state.animationSpec = ZoomAnimationSpec(
    durationMillis = 500,
    easing = LinearOutSlowInEasing,
    initialVelocity = 10f
)

// 或者在默认值的基础上修改部分参数
state.animationSpec = ZoomAnimationSpec.Default.copy(durationMillis = 500)

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```