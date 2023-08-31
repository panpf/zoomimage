## Click/点击事件

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

ZoomImage needs to receive double-click events, so it has to intercept click and long press events,
and you can receive both events through the provided interface
<br>-----------</br>
ZoomImage 需要接收双击事件，所以不得已也拦截了单击和长按事件，你可以通过提供的接口来接收这两个事件

example/示例：

compose：

```kotlin
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    onTap = { touch: Offset ->
        // Click Events,单击事件
    },
    onLongPress = { touch: Offset ->
        // Long press event,长按事件        
    },
)
```

view：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomAbility.registerOnViewTapListener { view: android.view.View, x: Float, y: Float ->
    // Click Events,单击事件
}

sketchZoomImageView.zoomAbility.registerOnViewLongPressListener { view: android.view.View, x: Float, y: Float ->
    // Long press event,长按事件        
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt