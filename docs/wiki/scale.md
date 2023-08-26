## Scale Image/缩放图像

zoomimage 库支持双击缩放，手势缩放，scale() 等多种缩放方式来缩放图像。

### minScale, mediumScale, maxScale

zoomimage 在缩放的过程中始终受到 minScale 和 maxScale 的约束，minScale 和 maxScale 分别表示最小缩放倍数和最大缩放倍数。

* minScale：最小缩放倍数，用于限制 zoomimage 在缩放过程中的最小值，取值始终是 ContentScale.computeScaleFactor(srcSize, dstSize).scaleX 的值
* mediumScale：中间缩放倍数，专门用于双击缩放，取值受 scalesCalculator 参数控制
* maxScale：最大缩放倍数，用于限制 zoomimage 在缩放过程中的最大值，取值受 scalesCalculator 参数控制


#### ScalesCalculator

[//]: # (todo contine)

ScalesCalculator.Dynamic，计算规则如下：

* mediumScale 是在以下几个值中取最大值：
  * minMediumScale：minScale * stepScaleMultiple，stepScaleMinMultiple 默认值为 3f
  * fillContainerScale：完全充满容器时的缩放倍数，效果类似 ContentScale.Crop
* maxScale 始终是 mediumScale * minStepScaleMultiple

修改 scalesCalculator：

```kotlin
val state: ZoomState by rememberZoomState()
state.zoomable.scalesCalculator = ScalesCalculator.fixed(multiple = 4f)

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### 双击缩放

双击缩放是指双击图片时，图片会缩放到下一个缩放倍数，默认总是在 minScale 和 mediumScale 之间循环

如果你想在 minScale、mediumScale 以及 maxScale 之间循环，可以修改 threeStepScale 为 true，如下：

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

stepScaleMinMultiple
rubberBandScale
animationSpec
minScale
mediumScale
maxScale