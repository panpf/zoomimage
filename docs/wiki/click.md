## Click Events

Translations: [简体中文](click_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage needs to receive double-click events, so it has to intercept click and long press events,
and you can receive both events through the provided interface

### Examples

compose：

```kotlin
SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    onTap = { touch: Offset ->
        // Click Events
    },
    onLongPress = { touch: Offset ->
        // Long press event    
    },
)
```

view：

View's setOnClickListener and setOnLongClickListener methods are still available
Additional OnViewTapListener and OnViewLongPressListener interfaces with touch location are provided

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.onViewTapListener = { view: android.view.View, x: Float, y: Float ->
    // Click Events
}

sketchZoomImageView.onViewLongPressListener = { view: android.view.View, x: Float, y: Float ->
    // Long press event      
}
```

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt