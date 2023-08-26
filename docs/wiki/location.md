## Location 图像/定位图像

> * The following example takes precedence over the Compose version of the ZoomImage component for demonstration
> * The API of ZoomImageView is exactly the same as ZoomImage, except that the entrance is different
> * ZoomState.zoomable is equivalent to ZoomImageView.zoomAbility
> * ZoomState.subsampling is equivalent to ZoomImageView.subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的 ZoomImage 组件来演示
> * ZoomImageView 的 API 和 ZoomImage 一模一样，只是入口不一样
> * ZoomState.zoomable 等价于 ZoomImageView.zoomAbility
> * ZoomState.subsampling 等价于 ZoomImageView.subsamplingAbility

### location()

zoomimage 提供改了 location() 方法用来定位到图像的指定位置，指定的位置一定会显示在屏幕的中间，它有两个参数：

* contentPoint: IntOffset。content 上的定位点，原点是 content 的左上角
* targetScale: Float = transform.scaleX。目标缩放倍数，默认是当前缩放倍数
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
        // 定位到 content 的中心，如果当前缩放倍数小于 mediumScale，就缩放到 mediumScale
        state.zoomable.location(
            contentPoint = state.zoomable.contentSize.center,
            targetScale = state.zoomable.transform.scaleX.coerceAtLeast(state.zoomable.mediumScale),
            animated = true,
        )
    }
) {
    Text(text = "location to center")
}
```