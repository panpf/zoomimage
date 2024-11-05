## Subsampling

Translations: [简体中文](subsampling_zh.md)

> [!TIP]
> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

Some images are huge in size, if they are read completely into memory will make the app
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
* The scaling factor of the sides of contentSize and contentOriginSize does not differ by more than
  1f
* On Android, it must be a type supported by BitmapRegionDecoder, non-Android platforms are fine as
  long as they are not GIFs

### Use the subsampling feature

Components that integrate the image loading library can use the subsampling function without any
additional work

[ZoomImage] and [ZoomImageView] do not have an integrated image loading library and require an
additional call to the `setSubsamplingImage()` method to use the subsampling function

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchedEffect(zoomState.subsampling) {
    val resUri = Res.getUri("files/huge_world.jpeg")
    val imageSource = ImageSource.fromComposeResource(resUri)
  zoomState.setSubsamplingImage(imageSource)
}
ZoomImage(
    painter = painterResource(Res.drawable.huge_world_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

view:

```kotlin
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_world_thumbnail)

val imageSource = ImageSource.fromResource(context, R.raw.huge_world)
zoomImageView.setSubsamplingImage(imageSource)
```

### ImageSource

[ImageSource] is responsible for providing image data to ZoomImage for decoding. ZoomImage provides
a variety of [ImageSource] implementations to support loading images from various sources, as
follows:

* [AssetImageSource]: Load images from Android's assets
  directory.[ImageSource.fromAsset(context, "huge_world.jpeg")][AssetImageSource]
* [ByteArrayImageSource]: Load images from
  ByteArray. [ImageSource.fromByteArray(byteArray)][ByteArrayImageSource]
* [ComposeResourceImageSource]: Load images from Compose's resource
  directory. [ImageSource.fromComposeResource(Res.getUri("files/huge_world.jpeg"))][ComposeResourceImageSource]
* [ContentImageSource]: Load images from Android's
  ContentProvider. [ImageSource.fromContent(context, contentUri)][ContentImageSource]
* [FileImageSource]: Load image from file. [ImageSource.fromFile(file)][FileImageSource]
* [KotlinResourceImageSource]: Load images from the Kotlin resource directory on desktop or ios
  platforms. [ImageSource.fromKotlinResource("huge_world.jpeg")][KotlinResourceImageSource]
* [ResourceImageSource]: Load images from Android's res
  directory. [ImageSource.fromResource(context, R.raw.huge_world)][ResourceImageSource]

### \*SubsamplingImageGenerator

The components of the Sketch, Coil, Glide, and Picasso series must create a SubsamplingImage based
on data or uri after the image is loaded successfully.
To support subsampling functionality, they all have their default SubsamplingImageGenerator
implementation

If the default implementation cannot correctly convert model or data to ImageSource when
creating [SubsamplingImage] or you need to intercept the creation process, then you can customize a
SubsamplingImageGenerator and apply it. The following takes the Sketch component as an example.
Other components are similar:

```kotlin
class MySketchComposeSubsamplingImageGenerator : SketchComposeSubsamplingImageGenerator {

  override fun generateImage(
    sketch: Sketch,
    request: ImageRequest,
    result: ImageResult.Success,
    painter: Painter
  ): SubsamplingImageGenerateResult? {
    // If the conditions are not met, skip the current SubsamplingImageGenerator
    if (true) {
      return null
    }

    // If the conditions are not met, the generation fails and a failure result is returned.
    if (true) {
      return SubsamplingImageGenerateResult.Error("message")
    }

    // Success
    val imageSource: ImageSource = ...
    val imageInfo: ImageInfo = ...
    val subsamplingImage = SubsamplingImage(imageSource, imageInfo)
    return SubsamplingImageGenerateResult.Success(subsamplingImage)
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return other != null && this::class == other::class
  }

  override fun hashCode(): Int {
    return this::class.hashCode()
  }

  override fun toString(): String {
    return "MySketchComposeSubsamplingImageGenerator"
  }
}

val subsamplingImageGenerators =
  remember { listOf(MySketchComposeSubsamplingImageGenerator()).toImmutableList() }
val sketchZoomState = rememberSketchZoomState(subsamplingImageGenerators)
SketchAsyncZoomImage(
  zoomState = sketchZoomState,
    ...
)


class MySketchViewSubsamplingImageGenerator : SketchViewSubsamplingImageGenerator {

  override fun generateImage(
    sketch: Sketch,
    request: ImageRequest,
    result: ImageResult.Success,
    drawable: Drawable
  ): SubsamplingImageGenerateResult? {
    // If the conditions are not met, skip the current SubsamplingImageGenerator
    if (true) {
      return null
    }

    // If the conditions are not met, the generation fails and a failure result is returned.
    if (true) {
      return SubsamplingImageGenerateResult.Error("message")
    }

    // Success
    val imageSource: ImageSource = ...
    val imageInfo: ImageInfo = ...
    val subsamplingImage = SubsamplingImage(imageSource, imageInfo)
    return SubsamplingImageGenerateResult.Success(subsamplingImage)
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return other != null && this::class == other::class
  }

  override fun hashCode(): Int {
    return this::class.hashCode()
  }

  override fun toString(): String {
    return "MySketchViewSubsamplingImageGenerator"
  }
}

val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.setSubsamplingImageGenerators(MySketchViewSubsamplingImageGenerator())
```

> [!TIP]
> If you customize mode or data, you must customize a SubsamplingImageGenerator and apply it,
> otherwise you will not be able to use the subsampling function

### Exif Orientation

By default, ZoomImage will read the Exif Orientation information of the image, and then rotate the
image, you can't disable it

### Tile Animation

ZoomImage supports transparency animation when displaying Tile. The animation is enabled by default,
with a duration of 200 milliseconds and a refresh interval of 8 milliseconds. You can pass
`tileAnimationSpec` parameters to turn off animation or modify animation duration and refresh
interval

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // Turn off animations
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec.None

    // Modify the duration and refresh interval of the animation
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
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
`pausedContinuousTransformTypes` property

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // All continuous transform types load tiles in real time
    zoomState.subsampling.pausedContinuousTransformTypes = 0

    // All continuous transform types pause loading of tiles
    zoomState.subsampling.pausedContinuousTransformTypes =
        TileManager.DefaultPausedContinuousTransformType or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Stop load tiles

ZoomImage supports stopping subsampling, which free the loaded tile after stopping and no new tiles
are loaded, and automatically reloads the tiles after restarting, you can configure it via
the `stopped` attribute

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // stop
    zoomState.subsampling.stopped = true
    // restart
    zoomState.subsampling.stopped = false
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

#### Lifecycle

By default, ZoomImage automatically fetches the most recent Lifecycle and listens to its state,
pausing or resuming subsampling at the Lifecycle stop or start

Get the latest Lifecycle in View through View.findViewTreeLifecycleOwner() API; in Compose, get
Lifecycle through LocalLifecycleOwner.current API

### Background tiles

ZoomImage uses background tiles to change sampleSize when switching sampleSize
The change in the clarity of the picture also changes step by step, and the basemap will not be
exposed during the process of loading new tiles, which ensures the continuity of the clarity change
and the user experience is better

However, this feature uses more memory, which may affect fluency on devices with poor performance,
and this feature is turned on by default, you can pass `disabledBackgroundTiles` property to close
it

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    zoomState.subsampling.disabledBackgroundTiles = true
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### Memory Cache

The subsampling feature supports memory caching, which can cache Bitmap in memory, which can avoid
repeated decoding and improve performance

Components that integrate the image loading library can use the memory caching feature without any
additional work, while components that do not integrate the image loading library need to implement
their own first
[TileImageCache] Then set the `tileImageCache` property to use the memory cache feature

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
  zoomState.subsampling.tileImageCache = MyTileImageCache()
}

ZoomImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

After setting the tileImageCache property, the memory caching function is turned on, and it can be
passed without modifying the tileImageCache property
The `disabledTileImageCache` property controls the use of the memory cache feature

example:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // Disable memory caching
  zoomState.subsampling.disabledTileImageCache = true
    // Memory caching is allowed
  zoomState.subsampling.disabledTileImageCache = false
}

ZoomImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### RegionDecoder

ZoomImage uses BitmapRegionDecoder on the Android platform to decode images, and non-Android
platforms use Skia to decode images, but they support limited image types. You can expand the
supported image types through the [RegionDecoder] interface.

First implement the [RegionDecoder] interface and its Factory interface to define
your [RegionDecoder],
refer to [SkiaRegionDecoder] and [AndroidRegionDecoder]

Then apply your [RegionDecoder] on [SubsamplingState] or [SubsamplingEngine] as follows:

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
  zoomState.subsampling.regionDecoders = listOf(MyRegionDecoder.Factory())
}

ZoomImage(
  imageUri = "https://sample.com/sample.jpeg",
  contentDescription = "view image",
  modifier = Modifier.fillMaxSize(),
  zoomState = zoomState,
)

val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.subsampling.regionDecodersState.value = listOf(MyRegionDecoder.Factory())
```

### Public Properties

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val subsampling: SubsamplingState = zoomState.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> [!TIP]
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

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[TileImageCache]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/TileImageCache.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[ImageSource]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/ImageSource.kt

[AssetImageSource]: ../../zoomimage-core/src/androidMain/kotlin/com/github/panpf/zoomimage/subsampling/AssetImageSource.kt

[ByteArrayImageSource]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/ByteArrayImageSource.kt

[ComposeResourceImageSource]: ../../zoomimage-compose-resources/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/ComposeResourceImageSource.kt

[ContentImageSource]: ../../zoomimage-core/src/androidMain/kotlin/com/github/panpf/zoomimage/subsampling/ContentImageSource.kt

[FileImageSource]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/FileImageSource.kt

[KotlinResourceImageSource]: ../../zoomimage-core/src/desktopMain/kotlin/com/github/panpf/zoomimage/subsampling/KotlinResourceImageSource.kt

[ResourceImageSource]: ../../zoomimage-core/src/androidMain/kotlin/com/github/panpf/zoomimage/subsampling/ResourceImageSource.kt

[SubsamplingImage]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/SubsamplingImage.kt

[RegionDecoder]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/RegionDecoder.kt

[SkiaRegionDecoder]: ../../zoomimage-core/src/nonAndroidMain/kotlin/com/github/panpf/zoomimage/subsampling/internal/SkiaRegionDecoder.kt

[AndroidRegionDecoder]: ../../zoomimage-core/src/androidMain/kotlin/com/github/panpf/zoomimage/subsampling/internal/AndroidRegionDecoder.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[SubsamplingEngine]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/view/subsampling/SubsamplingEngine.kt