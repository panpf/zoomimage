## 滚动条

翻译：[English](scrollbar.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

ZoomImage 支持显示滚动条，滚动条可以明确的告知用户当前的位置，还有多远距离到达底部或顶部。滚动条会在无操作
800 毫秒后自动隐藏，当用户再次操作时会自动显示

### 配置

[ScrollBarSpec] 用来描述滚动条的样式，有三个参数：

* `color: Color = Color(0xB2888888)`: 滚动条的颜色，默认为灰色。
* `size: Dp = 3.dp`: 滚动条的尺寸，默认为 3 dp。横向滚动条时为高度，纵向滚动条时为宽度。
* `margin: Dp = 6.dp`: 滚动条到边缘的距离，默认为 6 dp。

compose:

```kotlin
val scrollBar = remember {
    ScrollBarSpec(
        color = androidx.compose.ui.graphics.Color.Red,
        size = 6.dp,
        margin = 12.dp,
    )
}
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    scrollBar = scrollBar,
)
```

view:

```kotlin
val sketchImageView = SketchZoomImageView(context)
sketchImageView.scrollBar = ScrollBarSpec(
    color = androidx.compose.ui.graphics.Color.Red,
    size = 6.dp,
    margin = 12.dp,
)
```

### 关闭滚动条

ZoomImage 默认显示滚动条，你可以关闭它

compose:

```kotlin
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    scrollBar = null,
)
```

view:

```kotlin
val sketchImageView = SketchZoomImageView(context)
sketchImageView.scrollBar = null
```

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ScrollBarSpec]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ScrollBarSpec.kt