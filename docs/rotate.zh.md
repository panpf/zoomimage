## 旋转

翻译：[English](rotate.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

### rotate()

ZoomImage 提供了 `rotate()` 方法用来旋转图像到指定角度，它有一个参数：

* `targetRotation: Int`：目标旋转角度，它只能是 90 的倍数，比如 0、90、180、270、360 等

示例：

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

### 可访问属性

```kotlin
// compose
val zoomState: ZoomState by rememberSketchZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val zoomable: ZoomableState = zoomState.zoomable

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
```

> 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀


只读属性：

* `zoomable.transform.rotation: Float`: 当前旋转角度（基础旋转角度 + 用户旋转角度）
* `zoomable.baseTransform.rotation: Float`: 当前基础旋转角度，受 rotate() 方法影响
* `zoomable.userTransform.rotation: Float`: 当前用户旋转角度，一直为 0
* `zoomable.continuousTransformType: Int`: 当前正在进行的连续变换的类型
* `zoomable.contentBaseDisplayRectF: Rect`: content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseDisplayRect: IntRect`: content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseVisibleRectF: Rect`: content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentBaseVisibleRect: IntRect`: content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentDisplayRectF: Rect`: content 经过 transform 变换后在 container 中的区域
* `zoomable.contentDisplayRect: IntRect`: content 经过 transform 变换后在 container 中的区域
* `zoomable.contentVisibleRectF: Rect`: content 经过 transform 变换后自身对用户可见的区域
* `zoomable.contentVisibleRect: IntRect`: content 经过 transform 变换后自身对用户可见的区域
* `zoomable.sourceVisibleRectF: Rect`: contentVisibleRect 映射到原图上的区域
* `zoomable.sourceVisibleRect: IntRect`: contentVisibleRect 映射到原图上的区域

交互方法：

* `zoomable.rotate()`: 旋转 content 到指定的角度，角度只能是 90 的倍数
* `zoomable.rotateBy()`: 以增量的方式旋转 content 指定的角度，角度只能是 90 的倍数
* `zoomable.touchPointToContentPoint(): IntOffset`: 将触摸点转换为 content 上的点，原点是 content
  的左上角
* `zoomable.touchPointToContentPointF(): Offset`: 将触摸点转换为 content 上的点，原点是 content 的左上角
* `zoomable.sourceToDraw(Offset): Offset`: 将原图上的点转换为绘制时的点，原点是 container 的左上角
* `zoomable.sourceToDraw(Rect): Rect`: 将原图上的矩形转换为绘制时的矩形，原点是 container 的左上角

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt