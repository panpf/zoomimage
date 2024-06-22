## Subsampling

Translations: [简体中文](subsampling_zh.md)

> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

Some images are huge in size, if they are read completely into memory will definitely make the app
crash due to insufficient memory, the image loading framework will usually sample and then load,
then the size of the image will become smaller, but the content of the image will also become blurry

Therefore, it is necessary that ZoomImage can support subsampling when zooming, and the user can
subsampling wherever he slides, and then display the clear original image fragments on the screen,
so that it can display a clear picture when zooming without crashing the app

### Features

* [Exif Orientation](#exif-orientation). Support reading the Exif Orientation information of the
  image and then rotating the image
* [Tile Animation](#tile-animation). Support transparency animation when displaying Tile, making
  the transition more natural
* [Background tiles](#background-tiles). When switching sampleSize, the picture
  clarity changes step by step, making the transition more natural.
* [Pause load tiles](#pause-load-tiles). Pause loading of tiles
  during continuous transformations to improve performance
* [Stop load tiles](#stop-load-tiles). Listen to Lifecycle, stop loading tiles and release loaded
  tiles at stop to improve performance
* [Memory cache](#memory-cache). Avoid repeated decoding and improve performance
* [Public Properties](#public-properties). Can read sampling size, picture
  information, tile list and other information

### Prefix

When will subsampling be enabled?

* contentSize is smaller than contentOriginSize
* The aspect ratio of contentSize and the aspect ratio of contentOriginSize do not differ by more
  than 0.5f
* On Android, it must be a type supported by BitmapRegionDecoder. If the desktop platform is not
  GIF, it is fine.

### Use the subsampling feature

Components that integrate the image loading library can use the subsampling function without any
additional work

[ZoomImage] and [ZoomImageView] do not have an integrated image loading library and require an
additional call to the `setImageSource(ImageSource)` method to use the subsampling function

example：

```kotlin
val state: ZoomState by rememberZoomState()

val context = LocalContext.current
LaunchedEffect(Unit) {
    state.subsampling.setImageSource(ImageSource.fromResource(context, R.drawable.huge_image))
}

ZoomImage(
    painter = painterResource(R.drawable.huge_image_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

view:

```kotlin
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_image_thumbnail)

val imageSource = ImageSource.fromResource(context, R.drawable.huge_image)
zoomImageView.subsampling.setImageSource(imageSource)
```

### Exif Orientation

By default, ZoomImage will read the Exif Orientation information of the image, and then rotate the
image, you can't disable it

### Tile Animation

ZoomImage supports transparency animation when displaying Tile. The animation is enabled by default,
with a duration of 200 milliseconds and a refresh interval of 8 milliseconds. You can pass
`tileAnimationSpec` parameters to turn off animation or modify animation duration and refresh
interval

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turn off animations
    state.subsampling.tileAnimationSpec = TileAnimationSpec.None

    // Modify the duration and refresh interval of the animation
    state.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Pause load tiles

ZoomImage divides the continuous transformation behavior into five
types: `SCALE`, `OFFSET`, `LOCATE`, `GESTURE`, `FLING`,
and supports configuring the specified type of continuous transformation to pause
loading tiles, which can improve performance

The default configuration of ZoomImage is 'SCALE', 'OFFSET', 'LOCATE' three types of continuous
transformations that pause the loading of tiles, 'GESTURE',
The 'FLING' two types load tiles in real time, which you can configure via the
`pausedContinuousTransformType` property

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // All continuous transform types load tiles in real time
    state.subsampling.pausedContinuousTransformType = ContinuousTransformType.NONE

    // All continuous transform types pause loading of tiles
    state.subsampling.pausedContinuousTransformType =
        TileManager.DefaultPausedContinuousTransformType or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Stop load tiles

ZoomImage supports stopping subsampling, which free the loaded tile after stopping and no new tiles
are loaded, and automatically reloads the tiles after restarting, you can configure it via
the `stopped` attribute

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // stop
    state.subsampling.stopped = true
    // restart
    state.subsampling.stopped = false
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

#### Lifecycle

By default, ZoomImage automatically fetches the most recent Lifecycle and listens to its state,
pausing or resuming subsampling at the Lifecycle stop or start

If it is in a fragment, it will automatically get the Fragment's Lifecycle, no need to actively set
it, compose and view can be used. Behind the dependence
The LocalLifecycleOwner API for compos, and the View.findViewTreeLifecycleOwner() API in the
Lifecycle KTX package

If in a special operating environment, the above API cannot obtain Lifecycle or the Lifecycle
obtained by default does not meet the requirements, you can also use `stoppedController` property
sets your Lifecycle

example：

```kotlin
val state: ZoomState by rememberZoomState()

val lifecycle: Lifecycle = ...
LaunchedEffect(lifecycle) {
    state.subsampling.stoppedController = LifecycleStoppedController(lifecycle)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Background tiles

ZoomImage uses background tiles to change sampleSize when switching sampleSize
The change in the clarity of the picture also changes step by step, and the basemap will not be
exposed during the process of loading new tiles, which ensures the continuity of the clarity change
and the user experience is better

However, this feature uses more memory, which may affect fluency on devices with poor performance,
and this feature is turned on by default, you can pass `disabledBackgroundTiles` property to close
it

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.subsampling.disabledBackgroundTiles = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Memory Cache

The subsampling feature supports memory caching, which can cache Bitmap in memory, which can avoid
repeated decoding and improve performance

Components that integrate the image loading library can use the memory caching feature without any
additional work, while components that do not integrate the image loading library need to implement
their own first
[TileBitmapCache] Then set the `tileBitmapCache` property to use the memory cache feature

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.subsampling.tileBitmapCache = MyTileBitmapCache()
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

After setting the tileBitmapCache property, the memory caching function is turned on, and it can be
passed without modifying the tileBitmapCache property
The `disabledTileBitmapCache` property controls the use of the memory cache feature

example：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Disable memory caching
    state.subsampling.disabledTileBitmapCache = true
    // Memory caching is allowed
    state.subsampling.disabledTileBitmapCache = false
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Public Properties

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(state = state)
val subsampling: SubsamplingState = state.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> * Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
    suffixed with State compared to the compose version

* `subsampling.ready: Boolean`: Whether the image is ready for subsampling
* `subsampling.imageInfo: ImageInfo`: The information of the image, including width, height, format,
  exif information, etc
* `subsampling.exifOrientation: ExifOrientation`: The exif information of the image
* `subsampling.foregroundTiles: List<TileSnapshot>`: List of current foreground tiles
* `subsampling.backgroundTiles: List<TileSnapshot>`: List of current background tiles
* `subsampling.sampleSize: Int`: The sample size of the image
* `subsampling.imageLoadRect: IntRect`: The image load rect
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`: Tile grid size map

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[TileBitmapCache]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/TileBitmapCache.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt