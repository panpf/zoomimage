## 偏移

翻译：[English](offset.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

ZoomImage 支持单指拖动、惯性滑动、键盘拖动，以及 `offset()` 方法来移动图像。

### 单指拖动

ZoomImage 默认开启单指拖动手势，但你可以关闭它，如下：

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.ONE_FINGER_DRAG
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 键盘拖动

ZoomImage 支持通过键盘拖动图像，支持短按和长按两种操作。默认注册了以下按键：

* move up: Key.DirectionUp
* move down: Key.DirectionDown
* move left: Key.DirectionLeft
* move right: Key.DirectionRight

由于键盘拖动功能必须依赖焦点，而焦点管理又非常复杂，所以默认没有开启它，需要你主动配置并请求焦点，如下：

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
> 在 HorizontalPager 中请求焦点时需要注意只能为当前页请求焦点，否则会导致意想不到的意外

你还可以通过手势控制动态关闭它，如下：

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchEffect(zoomState.zoomable) {
    zoomState.zoomable.disabledGestureTypes =
        zoomState.zoomable.disabledGestureTypes or GestureType.KEYBOARD_DRAG
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### offset()

ZoomImage 提供了 `offset()` 方法用来移动图像到指定位置，它有两个参数：

* `targetOffset: Offset`: 目标偏移位置，offset 原点是组件的左上角
* `animated: Boolean = false`: 是否使用动画，默认为 false

示例：

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
            val targetOffset = zoomState.zoomable.transform.offset + Offset(x = 100, y = 200)
            zoomState.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset + Offset(100, 200)")
}

Button(
    onClick = {
        coroutineScope.launch {
            val targetOffset = zoomState.zoomable.transform.offset - Offset(x = 100, y = 200)
            zoomState.zoomable.offset(targetOffset = targetOffset, animated = true)
        }
    }
) {
    Text(text = "offset - Offset(100, 200)")
}
```

### 限制偏移边界

ZoomImage 默认不管你设置的是什么 [ContentScale]
都可以拖动查看图像的全部内容，例如你设置了 [ContentScale] 为 Crop，[Alignment] 为
Center，那么默认只显示图像中间的部分，然后你还可以单指或双指拖动来查看图像的全部内容

如果你希望图像只能在 [ContentScale] 和 [Alignment] 所限制的区域内移动，不能查看全部内容，这时你可以修改
`limitOffsetWithinBaseVisibleRect` 参数为 true 来达到此目的

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    zoomState.zommable.limitOffsetWithinBaseVisibleRect = true
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 容器空白区域

ZoomImage 默认在拖动图像时图像的边缘始终和容器的边缘对齐，它们中间不会存在空白区域（图像初始状态时除外），当你需要在图像和容器之间留有空白区域时，你可以通过设置
`containerWhitespace` 或 `containerWhitespaceMultiple` 参数为来达到此目的

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.zommable) {
    // 通过 containerWhitespace 属性设置具体的大小
    zoomState.zommable.containerWhitespace = ContainerWhitespace(
        left = 4f, top = 3f, right = 2f, bottom = 1f
    )
    // or
    zoomState.zommable.containerWhitespace = ContainerWhitespace(horizontal = 2f, vertical = 1f)
    // or
    zoomState.zommable.containerWhitespace = ContainerWhitespace(size = 1f)

    // 在图像边缘和容器边缘之间留有 50% 容器大小的空白区域
    zoomState.zommable.containerWhitespaceMultiple = 0.5f
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 可访问属性

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
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.transform.offset: Offset`: 当前偏移量（baseTransform.offset + userTransform.offset）
* `zoomable.baseTransform.offset: Offset`: 当前基础偏移量，受 alignment 参数和 rotate 方法影响
* `zoomable.userTransform.offset: Offset`: 当前用户偏移量，受 offset()、locate() 以及用户手势拖动影响
* `zoomable.scrollEdge: ScrollEdge`: 当前偏移的边界状态

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[ScrollEdge]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/zoom/ScrollEdge.kt