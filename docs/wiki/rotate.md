## Rotate Image/旋转图像

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

### rotate()

ZoomImage provides a modified rotate() method to rotate the image to a specified angle, which has
one parameter:
<br>-----------</br>
ZoomImage 提供了 rotate() 方法用来旋转图像到指定角度，它有一个参数：

* targetRotation: Int。
    * Target rotation angle, which can only be a multiple of 90, such as 0, 90, 180, 270, 360, etc
      <br>-----------</br>
    * 目标旋转角度，它只能是 90 的倍数，比如 0、90、180、270、360 等

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
            val targetRotation = state.zoomable.transform.rotation.roundToInt() + 90
            state.zoomable.rotate(targetRotation = targetRotation)
        }
    }
) {
    Text(text = "right rotate 90")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetRotation = state.zoomable.transform.rotation.roundToInt() - 90
            state.zoomable.rotate(targetRotation = targetRotation)
        }
    }
) {
    Text(text = "left rotate 90")
}
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

* `zoomable.transform.rotation: Float`。
    * Current rotation angle (base rotation angle + user rotation angle)
      <br>-----------</br>
    * 当前旋转角度（基础旋转角度 + 用户旋转角度）
* `zoomable.baseTransform.rotation: Float`。
    * The current base rotation angle, affected by the rotate() method
      <br>-----------</br>
    * 当前基础旋转角度，受 rotate() 方法影响
* `zoomable.userTransform.rotation: Float`。
    * The current user rotation angle, which is always 0
      <br>-----------</br>
    * 当前用户旋转角度，一直为 0

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