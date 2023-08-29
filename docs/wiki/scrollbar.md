## Scrollbar/滚动条

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is
    different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomAbility
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomAbility
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsamplingAbility

ZoomImage supports displaying scroll bars, which can clearly tell the user where the current scroll
is and how far to reach the bottom or top. The scroll bar is automatically hidden after 800
milliseconds of no action and automatically appears when the user takes action again
<br>-----------</br>
zoomimage 支持显示滚动条，可以明确的告知用户当前的滚动位置，还有多远距离到达底部或顶部。滚动条会在无操作
800 毫秒后自动隐藏，当用户再次操作时会自动显示

### Configure the scroll bar/配置滚动条

[ScrollBarSpec] is used to describe the style of the scroll bar and has three parameters:
<br>-----------</br>
[ScrollBarSpec] 用来描述滚动条的样式，有三个参数：

* color: Color = Color(0xB2888888)。
    * The color of the scroll bar, which defaults to gray.
      <br>-----------</br>
    * 滚动条的颜色，默认为灰色。
* size: Dp = 3.dp。
    * The size of the scroll bar, which defaults to 3 dp. The height for the horizontal scroll bar
      and the width for the vertical scroll bar.
      <br>-----------</br>
    * 滚动条的尺寸，默认为 3 dp。横向滚动条时为高度，纵向滚动条时为宽度。
* margin: Dp = 6.dp。
    * The distance of the scroll bar from the edge, which defaults to 6 dp.
      <br>-----------</br>
    * 滚动条距离边缘的距离，默认为 6 dp。

example/示例：

```kotlin
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    scrollBar = ScrollBarSpec(
        color = androidx.compose.ui.graphics.Color.Red,
        size = 6.dp,
        margin = 12.dp,
    ),
)
```

### Close the scroll bar/关闭滚动条

ZoomImage displays the scroll bar by default, and you can turn it off
<br>-----------</br>
zoomimage 默认显示滚动条，你可以关闭它

example/示例：

```kotlin
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    scrollBar = null,
)
```

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[ScrollBarSpec]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/zoom/ScrollBarSpec.kt