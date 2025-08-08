## Keep Transform

Translations: [简体中文](keep_transform.zh.md)

ZoomImage will directly reset the transformation state when the following properties are changed:

* rotation
* contentScale
* alignment
* readMode
* scalesCalculator
* limitOffsetWithinBaseVisibleRect
* containerWhitespaceMultiple
* containerWhitespace

### containerSize Changed

Only when the containerSize property changes, ZoomImage will restore the transform state according
to the new containerSize and the current transform state, ensuring that the center point currently
visible to the user is always in the center of the screen. This way, when you change the window size
on the desktop platform, ZoomImage will automatically adapt to the new window size and keep the
visible center unchanged, and the visible range will also change.

> [!TIP]
> When the containerSize property changes while the above other properties also change, ZoomImage
> will directly reset the transformation state

### contentSize changed

By default, ZoomImage will reset the transform state directly only when the contentSize attributes
changed.

But after you set the `keepTransformWhenSameAspectRatioContentSizeChanged` property to true,
ZoomImage will restore the transform state, ensuring that the range visible to the user remains
unchanged. In this way, the thumbnail is displayed first, then the user operates the transformation,
and finally displays the original image, the transformation state will not be reset.

The `keepTransformWhenSameAspectRatioContentSizeChanged` property only works when switching images
with the same aspect ratio, so this feature is only suitable for scenes when switching thumbnails
and original images, so it is not enabled by default.

```kotlin
val zoomState: ZoomState by rememberSketchZoomState()

zoomState.zoomable.setKeepTransformWhenSameAspectRatioContentSizeChanged(true)

SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

> [!TIP]
> When the contentSize property changes, the above other properties also
> change, ZoomImage will directly reset the transformation state