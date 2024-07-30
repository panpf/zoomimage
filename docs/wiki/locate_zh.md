## 定位

翻译：[English](locate.md)

> [!TIP]
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
        // 定位到 content 的中心，如果当前缩放倍数小于 mediumScale，就缩放到 mediumScale
        coroutineScope.launch {
            zoomState.zoomable.locate(
                contentPoint = zoomState.zoomable.contentSize.center,
                targetScale = zoomState.zoomable.transform.scaleX.coerceAtLeast(zoomState.zoomable.mediumScale),
                animated = true,
            )
        }
    }
) {
    Text(text = "locate to center")
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt