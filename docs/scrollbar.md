## Scrollbar/滚动条

Translations: [简体中文](scrollbar.zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

ZoomImage supports displaying scroll bars that clearly tell the user where they are and how far they
have reached the bottom or top. The scroll bar will have no action
Auto-hide after 800 milliseconds and automatically displayed when the user takes action again

### Configure

[ScrollBarSpec] is used to describe the style of the scroll bar and has three parameters:

* `color: Color = Color(0xB2888888)`: The color of the scroll bar, which defaults to gray.
* `size: Dp = 3.dp`: The size of the scroll bar, which defaults to 3 dp. The height for the
  horizontal
  scroll bar and the width for the vertical scroll bar.
* `margin: Dp = 6.dp`: The distance of the scroll bar from the edge, which defaults to 6 dp.
* `enabledWindowInsets: Boolean = false`: Whether to enable WindowInsets, default is false

compose:

```kotlin
val scrollBar = remember {
    ScrollBarSpec(
        color = androidx.compose.ui.graphics.Color.Red,
        size = 6.dp,
        margin = 12.dp,
      enabledWindowInsets = true,
    )
}
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
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
  enabledWindowInsets = true,
)
```

ScrollBarSpec also provides some common configurations, as follows:

* ScrollBarSpec.Default: Default configuration, color gray, size 3 dp, 6 dp from edge, no
  WindowInsets enabled
* ScrollBarSpec.DefaultAndWindowInsets: Default configuration, color gray, size 3 dp, 6 dp from
  edge, WindowInsets enabled

If you want to increase the distance of the scroll bar based on navigation WindowInsets, as follows:

```kotlin
val scrollBarSpec = ScrollBarSpec.Default
// compose
Box(
  modifier = Modifier
    .fillMaxSize()
    .windowInsetsPaddingWithScrollBar(NavigationBarDefaults.windowInsets, scrollBarSpec)
) {

}

// view
val view = ...
ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
  val windowInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
  val scrollBarInsets = scrollBarSpec.toInsets()
  view.updatePadding(
    right = windowInsets.right + scrollBarInsets.right,
    bottom = windowInsets.bottom + scrollBarInsets.bottom
  )
  insets
}
```

### Close the scroll bar

ZoomImage displays the scroll bar by default, and you can turn it off

compose:

```kotlin
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
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

[ZoomImageView]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[ScrollBarSpec]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ScrollBarSpec.kt