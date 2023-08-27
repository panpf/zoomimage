## Offset Image/移动图像

> * The following example takes precedence over the Compose version of the ZoomImage component for
    demonstration
> * The API of ZoomImageView is exactly the same as ZoomImage, except that the entrance is different
> * ZoomState.zoomable is equivalent to ZoomImageView.zoomAbility
> * ZoomState.subsampling is equivalent to ZoomImageView.subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的 ZoomImage 组件来演示
> * ZoomImageView 的 API 和 ZoomImage 一模一样，只是入口不一样
> * ZoomState.zoomable 等价于 ZoomImageView.zoomAbility
> * ZoomState.subsampling 等价于 ZoomImageView.subsamplingAbility

zoomimage 支持单指、双指、惯性滑动，以及 offset() 方法来移动图像。

### offset()

zoomimage 提供改了 offset() 方法用来移动图像到指定位置，它有两个参数：

* targetOffset: Offset。目标偏移位置，offset 原点是组件的左上角
* animated: Boolean = false。是否使用动画，默认为 false

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
        val targetOffset = state.zoomable.transform.offset + Offset(x = 100, y = 200)
        state.zoomable.offset(targetOffset = targetOffset, animated = true)
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        val targetOffset = state.zoomable.transform.offset - Offset(x = 100, y = 200)
        state.zoomable.scale(targetScale = targetScale, animated = true)
    }
) {
    Text(text = "offset - Offset(100, 200)")
}
```

### 限制移动范围

zoomimage 默认不管你设置的是什么 ContentScale 都可以拖动查看图像的全部内容

例如你设置了 ContentScale 为 Crop，Alignment 为 Center，那么默认只显示图像中间的部分，然后你还可以单指或双指拖动来查看图像的全部内容

如果这不是你想要的，你希望图像只能在 ContentScale 和 Alignment 所限制的区域内移动，不能查看全部内容，这时你可以修改
limitOffsetWithinBaseVisibleRect 参数为 true 来达到此目的，如下：

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

### 获取相关信息

* ZoomableState.transform.offset: Offset。当前偏移量（baseTransform.offset + userTransform.offset）
* ZoomableState.baseTransform.offset: Offset。当前基础偏移量，受 alignment 参数和 rotate 方法影响
* ZoomableState.userTransform.offset: Offset。当前用户偏移量，受 offset()、location() 以及用户手势拖动影响
* ZoomableState.scrollEdge: ScrollEdge。当前偏移状态的边界信息，例如是否到达左边界、右边界、上边界、下边界等