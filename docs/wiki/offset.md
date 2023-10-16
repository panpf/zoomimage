## Offset Image/移动图像

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

ZoomImage supports one-finger drag, two-finger drag, inertial swipe, and the offset() method to move
the image.
<br>-----------</br>
ZoomImage 支持单指拖动、双指拖动、惯性滑动，以及 offset() 方法来移动图像。

### offset()

ZoomImage provides a modified offset() method to move the image to a specified position, which has
two parameters:
<br>-----------</br>
ZoomImage 提供了 offset() 方法用来移动图像到指定位置，它有两个参数：

* targetOffset: Offset。
    * The target offset, with the offset origin being the upper-left corner of the component
      <br>-----------</br>
    * 目标偏移位置，offset 原点是组件的左上角
* animated: Boolean = false。
    * Whether to use animation, the default is false
      <br>-----------</br>
    * 是否使用动画，默认为 false

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
            val targetOffset = state.zoomable.transform.offset + Offset(x = 100, y = 200)
            state.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = state.zoomable.transform.offset - Offset(x = 100, y = 200)
            state.zoomable.offset(targetScale = targetScale, animated = true)
        }
    }
) {
    Text(text = "offset - Offset(100, 200)")
}
```

### Limit the bounds of offset/限制平移边界

By default, zoomImage can drag to view the entire content of the image regardless of what you
set [ContentScale], for example, if you set [ContentScale] to Crop and [Alignment] to Center, then
only the middle part of the image is displayed by default, and then you can also drag with one or
two fingers to view the entire content of the image
<br>-----------</br>
ZoomImage 默认不管你设置的是什么 [ContentScale]
都可以拖动查看图像的全部内容，例如你设置了 [ContentScale] 为 Crop，[Alignment] 为
Center，那么默认只显示图像中间的部分，然后你还可以单指或双指拖动来查看图像的全部内容

If you want the image to be moved only within the area restricted
by [ContentScale] and [Alignment], and not the entire content, you can modify the
limitOffsetWithinBaseVisibleRect parameter to true to achieve this
<br>-----------</br>
如果你希望图像只能在 [ContentScale] 和 [Alignment] 所限制的区域内移动，不能查看全部内容，这时你可以修改
limitOffsetWithinBaseVisibleRect 参数为 true 来达到此目的

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.limitOffsetWithinBaseVisibleRect = true

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

* `zoomable.transform.offset: Offset`。
    * Current offset (baseTransform.offset + userTransform.offset)
      <br>-----------</br>
    * 当前偏移量（baseTransform.offset + userTransform.offset）
* `zoomable.baseTransform.offset: Offset`。
    * The current base offset, affected by the alignment parameter and the rotate method
      <br>-----------</br>
    * 当前基础偏移量，受 alignment 参数和 rotate 方法影响
* `zoomable.userTransform.offset: Offset`。
    * The current user offset, affected by offset(), locate(), and user gesture dragging
      <br>-----------</br>
    * 当前用户偏移量，受 offset()、locate() 以及用户手势拖动影响
* `zoomable.scrollEdge: ScrollEdge`。
    * Edge state for the current offset
      <br>-----------</br>
    * 当前偏移的边界状态

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

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScrollEdge]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScrollEdge.kt