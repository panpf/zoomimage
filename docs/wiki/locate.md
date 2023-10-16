## Locate 图像/定位图像

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

### locate()

ZoomImage provides a modified locate() method to locate the specified position of the image,
which is displayed in the middle of the screen (except for the edge position), which has three
parameters:
<br>-----------</br>
ZoomImage 提供改了 locate() 方法用来定位到图像的指定位置，指定的位置会显示在屏幕的中间（边缘位置除外），它有三个参数：

* contentPoint: IntOffset。
    * The anchor on the content, the origin is the upper-left corner of the content
      <br>-----------</br>
    * content 上的定位点，原点是 content 的左上角
* targetScale: Float = transform.scaleX。
    * The target magnification, which defaults to the current magnification
      <br>-----------</br>
    * 目标缩放倍数，默认是当前缩放倍数
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
        // Locate to the center of the content and zoom to mediumScale if the current zoom factor is less than MediumScale
        // 定位到 content 的中心，如果当前缩放倍数小于 mediumScale，就缩放到 mediumScale
        coroutineScope.launch {
            state.zoomable.locate(
                contentPoint = state.zoomable.contentSize.center,
                targetScale = state.zoomable.transform.scaleX.coerceAtLeast(state.zoomable.mediumScale),
                animated = true,
            )
        }
    }
) {
    Text(text = "locate to center")
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt