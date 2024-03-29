## 定位

翻译：[English](locate.md)

> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

### locate()

ZoomImage 提供改了 `locate()` 方法用来定位到图像的指定位置，指定的位置会显示在屏幕的中间（边缘位置除外），它有三个参数：

* `contentPoint: IntOffset`: content 上的定位点，原点是 content 的左上角
* `targetScale: Float` = transform.scaleX: 目标缩放倍数，默认是当前缩放倍数
* `animated: Boolean` = false: 是否使用动画，默认为 false

示例：

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