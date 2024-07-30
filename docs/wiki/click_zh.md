## 点击事件

翻译：[English](click.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

ZoomImage 需要接收双击事件，所以不得已也拦截了单击和长按事件，你可以通过提供的接口来接收这两个事件

### 示例

compose：

```kotlin
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    onTap = { touch: Offset ->
        // 单击事件
    },
    onLongPress = { touch: Offset ->
        // 长按事件        
    },
)
```

view：

View 的 setOnClickListener 和 setOnLongClickListener 方法依然可以使用，额外提供了带触摸位置的
OnViewTapListener 和 OnViewLongPressListener 接口

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.onViewTapListener = { view: android.view.View, x: Float, y: Float ->
    // 单击事件
}

sketchZoomImageView.onViewLongPressListener = { view: android.view.View, x: Float, y: Float ->
    // 长按事件        
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt