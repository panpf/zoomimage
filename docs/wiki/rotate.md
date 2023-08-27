## Rotate Image/旋转图像

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomAbility
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomAbility
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsamplingAbility

### rotate()

zoomimage 提供改了 rotate() 方法用来旋转图像到指定角度，它有一个参数：

* targetRotation: Int。目标旋转角度，它只能是 90 的倍数，比如 0、90、180、270、360 等

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
        val targetRotation = state.zoomable.transform.rotation.roundToInt() + 90
        state.zoomable.rotate(targetRotation = targetRotation)
    }
) {
    Text(text = "right rotate 90")
}

Button(
    onClick = {
        val targetRotation = state.zoomable.transform.rotation.roundToInt() - 90
        state.zoomable.rotate(targetRotation = targetRotation)
    }
) {
    Text(text = "left rotate 90")
}
```

### 获取相关信息

* [ZoomableState].transform.rotation: Float。当前旋转角度（基础旋转角度 + 用户旋转角度）
* [ZoomableState].baseTransform.rotation: Float。当前基础旋转角度，受 rotate() 方法影响
* [ZoomableState].userTransform.rotation: Float。当前用户旋转角度，一直为 0

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[ZoomableState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt