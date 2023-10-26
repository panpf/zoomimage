## Subsampling/子采样

> * The following example takes precedence over the Compose version component for demonstration
> * The API of [ZoomImageView] is exactly the same as [ZoomImage], except that the entrance is
    different
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomImageView] 的 API 和 [ZoomImage] 一模一样，只是入口不一样
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

Some images are huge in size, if they are read completely into memory will definitely make the app
crash due to insufficient memory, the image loading framework will usually sample and then load,
then the size of the image will become smaller, but the content of the image will also become blurry
<br>-----------</br>
有一些图片的尺寸巨大，如果把它们完整的读到内存肯定会让 App
因内存不足而崩溃，图片加载框架通常会采样后再加载，这时图片的尺寸会变小，但是图片的内容也会变的模糊不清

Therefore, it is necessary that ZoomImage can support subsampling when zooming, and the user can
subsampling wherever he slides, and then display the clear original image fragments on the screen,
so that it can display a clear picture when zooming without crashing the app
<br>-----------</br>
所以就需要 ZoomImage 在缩放时能够支持子采样，用户滑动到哪里就对哪里进行子采样，然后将清晰的原图图块显示到屏幕上，
这样就能够在缩放时既显示清晰的图片，又不会让 App 崩溃

### Features/特点

* [Exif Orientation](#exif-orientation). Support reading the Exif Orientation information of the
  image and then rotating the image
* [Animation](#tile-animation图块动画). Support transparency animation when displaying Tile, making
  the transition more natural
* [Background tiles](#disabledbackgroundtiles禁用背景图块). When switching sampleSize, the picture
  clarity changes step by step, making the transition more natural.
* [Pause load tiles](#pausedcontinuoustransformtype连续变换时暂停加载图块). Pause loading of tiles
  during continuous transformations to improve performance
* [Stop load tiles](#stoprestart). Listen to Lifecycle, stop loading tiles and release loaded tiles
  at stop to improve performance
* [Memory cache](#memory-cache). Avoid repeated decoding and improve performance
* [Reuse Bitmap](#reuse-bitmap). Avoid repeated creation of Bitmap, reduce memory jitter, and
  improve performance
* [Read information](#get-relevant-information获取相关信息). Can read sampling size, picture
  information, tile list and other information
* <br>-----------</br>
* [Exif Orientation](#exif-orientation). 支持读取图片的 Exif Orientation 信息，然后旋转图片
* [动画](#tile-animation图块动画). 在显示 Tile 的时候支持透明度动画，过渡更自然
* [背景图快](#disabledbackgroundtiles禁用背景图块). 切换 sampleSize 时图片清晰度逐级变化，过渡更自然
* [暂停加载图块](#pausedcontinuoustransformtype连续变换时暂停加载图块). 连续变换时暂停加载图块，提高性能
* [不可见时停止加载图块](#stoprestart). 监听 Lifecycle，在 stop 时停止加载图块并释放已加载的图块，提高性能
* [内存缓存](#memory-cache). 避免重复解码，提高性能
* [Bitmap 重用](#reuse-bitmap). 避免重复创建 Bitmap，减少内存抖动，提高性能
* [读取相关信息](#get-relevant-information获取相关信息). 可以读取采样大小、图片信息、图块列表等信息

### Prefix/前置条件

When will subsampling be enabled?
<br>-----------</br>
什么情况下才会开启子采样功能？

* contentSize is smaller than contentOriginSize
* The aspect ratio of contentSize and the aspect ratio of contentOriginSize do not differ by more
  than 0.5f
* The image is the type supported by BitmapRegionDecoder
  <br>-----------</br>
* contentSize 比 contentOriginSize 小
* contentSize 的宽高比和 contentOriginSize 的宽高比相差不超过 0.5f
* 图片是 BitmapRegionDecoder 支持的类型

### Use the subsampling feature/使用子采样功能

Components that integrate the image loading library can use the subsampling function without any
additional work
<br>-----------</br>
集成了图片加载库的组件无需任何额外的工作即可使用子采样功能

[ZoomImage] and [ZoomImageView] do not have an integrated image loading library and require an
additional call to the setImageSource() method to use the subsampling function
<br>-----------</br>
[ZoomImage] 和 [ZoomImageView] 没有集成图片加载库，需要额外调用 setImageSource() 方法以使用子采样功能

example/示例：

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
image, if you do not want ZoomImage to read the Exif Orientation information, you can modify the
ignoreExifOrientation parameter to true
<br>-----------</br>
ZoomImage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，如果你不想让 ZoomImage 读取 Exif
Orientation 信息，可以修改 ignoreExifOrientation 参数为 true

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.subsampling.ignoreExifOrientation = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Tile Animation/图块动画

By default, ZoomImage will read the Exif Orientation information of the image, and then rotate the
image, if you do not want ZoomImage to read the Exif Orientation information, you can modify the
ignoreExifOrientation parameter to true
<br>-----------</br>
ZoomImage 在显示 Tile 的时候支持透明度动画，默认开启动画，持续时间 200 毫秒，刷新间隔 8 毫秒，你可以通过
tileAnimationSpec 参数来关闭动画或修改动画的持续时间和刷新间隔

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Turn off animations
    // 关闭动画
    state.subsampling.tileAnimationSpec = TileAnimationSpec.None

    // Modify the duration and refresh interval of the animation
    // 修改动画的持续时间和刷新间隔
    state.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### pausedContinuousTransformType/连续变换时暂停加载图块

ZoomImage divides the continuous transformation behavior into five
types: `SCALE`, `OFFSET`, `LOCATE`, `GESTURE`, `FLING`,
and supports configuring the specified type of continuous transformation to pause
loading tiles, which can improve performance

The default configuration of ZoomImage is 'SCALE', 'OFFSET', 'LOCATE' three types of continuous
transformations that pause the loading of tiles, 'GESTURE',
The 'FLING' two types load tiles in real time, which you can configure via the
pausedContinuousTransformType property
<br>-----------</br>
ZoomImage 将连续变换行为分为 `SCALE`, `OFFSET`, `LOCATE`, `GESTURE`, `FLING`
五种类型，支持配置指定类型的连续变换暂停加载图块，这样可以提高性能

ZoomImage 在兼顾性能和体验的情况默认配置是 `SCALE`, `OFFSET`, `LOCATE`
三种类型的连续变换会暂停加载图块，`GESTURE`, `FLING` 两种类型会实时加载图块，
你可以通过 pausedContinuousTransformType 属性来配置它

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 所有连续变换类型都实时加载图块
    state.subsampling.pausedContinuousTransformType = ContinuousTransformType.NONE

    // 所有连续变换类型都暂停加载图块
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

### disabledBackgroundTiles/禁用背景图块

ZoomImage uses background tiles to change sampleSize when switching sampleSize
The change in the clarity of the picture also changes step by step, and the basemap will not be
exposed during the process of loading new tiles, which ensures the continuity of the clarity change
and the user experience is better

However, this feature uses more memory, which may affect fluency on devices with poor performance,
and this feature is turned on by default, you can turn it off
<br>-----------</br>
ZoomImage 通过背景图块实现了在切换 sampleSize 时随着 sampleSize
的变化图片清晰度也逐级变化的效果，并且在加载新图块的过程中也不会露出底图，这样就保证了清晰度变化的连续性，用户体验更好

但是此功能使用了更多的内存，在性能较差的设备上可能会对流畅性有影响，此功能默认开启，你可以关闭它

example/示例：

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

### stop/restart

ZoomImage supports stopping subsampling, which free the loaded tile after stopping and no new tiles
are loaded, and automatically reloads the tiles after restarting
<br>-----------</br>
ZoomImage 支持停止子采样，停止后会释放已加载的图块并不再加载新图块，重启后自动重新加载图块

example/示例：

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
<br>-----------</br>
ZoomImage 默认会自动获取最近的 Lifecycle 然后监听它的状态，在 Lifecycle stop 或 start 时停止或重启子采样

If it is in a fragment, it will automatically get the Fragment's Lifecycle, no need to actively set
it, compose and view can be used. Behind the dependence
The LocalLifecycleOwner API for compos, and the View.findViewTreeLifecycleOwner() API in the
Lifecycle KTX package
<br>-----------</br>
如果是在 Fragment 中就会自动获取到 Fragment 的 Lifecycle，无需主动设置，compose 和 view 都可以。背后依赖的是
compos 的 LocalLifecycleOwner API 和 Lifecycle KTX 包中的 View.findViewTreeLifecycleOwner() API

If the Lifecycle cannot be obtained by the above APIs in a special running environment, or the
Lifecycle obtained by default does not meet the requirements, you can also set it manually
Lifecycle
<br>-----------</br>
如果是在特殊的运行环境中，上述 API 无法获取到 Lifecycle 或者默认获取的 Lifecycle 不满足要求，还可以手动设置
Lifecycle

example/示例：

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

### Memory Cache

The subsampling feature supports memory caching, which can cache Bitmap in memory, which can avoid
repeated decoding and improve performance
<br>-----------</br>
子采样功能支持内存缓存，可以将 Bitmap 缓存在内存中，这样可以避免重复解码，提高性能

Components that integrate the image loading library can use the memory caching feature without any
additional work, while components that do not integrate the image loading library need to implement
their own first
[TileBitmapCache] Then set the tileBitmapCache property to use the memory cache feature
<br>-----------</br>
集成了图片加载库的组件无需任何额外的工作即可使用内存缓存功能，而没有集成图片加载库的组件需要先实现自己的
[TileBitmapCache] 然后设置 tileBitmapCache 属性才能使用内存缓存功能

example/示例：

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
The disabledTileBitmapCache property controls the use of the memory cache feature
<br>-----------</br>
设置了 tileBitmapCache 属性后就开启了内存缓存功能，还可以在不修改 tileBitmapCache 属性的情况下通过
disabledTileBitmapCache 属性控制使用内存缓存功能

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Disable memory caching, 禁用内存缓存
    state.subsampling.disabledTileBitmapCache = true
    // Memory caching is allowed, 允许使用内存缓存
    state.subsampling.disabledTileBitmapCache = false
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Reuse Bitmap

> Only Android is supported/仅支持 Android

The subsampling feature supports the reuse of Bitmaps, and new fragments can be decoded using
existing Bitmaps, which reduces the creation of Bitmaps, reduces memory jitter, and improves
performance
<br>-----------</br>
子采样功能支持重用 Bitmap，可以使用已经存在的 Bitmap 解码新的图块，这样可以减少创建 Bitmap，减少内存抖动，提高性能

Because only Sketch and Glide have BitmapPool, only components that integrate these two image
loading libraries can be reused without any additional work
Bitmap functionality, other components need to implement their own [TileBitmapPool] and then set the
tileBitmapPool property to use memory reuse
Bitmap functionality
<br>-----------</br>
因为只有 Sketch 和 Glide 有 BitmapPool，所以只有集成了这两个图片加载库的组件无需任何额外的工作即可使用重用
Bitmap 功能，其它组件需要先实现自己的 [TileBitmapPool] 然后设置 tileBitmapPool 属性才能使用重用
Bitmap 功能

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    state.subsampling.tileBitmapPool = MyTileBitmapPool()
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

After setting the tileBitmapPool property, the memory reuse Bitmap function is turned on, and it can
also be passed without modifying the tileBitmapPool property
The disabledTileBitmapReuse property controls the reuse of Bitmap
<br>-----------</br>
设置了 tileBitmapPool 属性后就开启了重用 Bitmap 功能，还可以在不修改 tileBitmapPool 属性的情况下通过
disabledTileBitmapReuse 属性控制重用 Bitmap

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // Disabled reuse of Bitmaps，禁止重用 Bitmap
    state.subsampling.disabledTileBitmapReuse = true
    // Allows reuse of Bitmap，允许重用 Bitmap
    state.subsampling.disabledTileBitmapReuse = false
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Get relevant information/获取相关信息

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
val subsampling: SubsamplingState = state.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> * Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
    suffixed with State compared to the compose version
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `subsampling.ready: Boolean`。
    * Whether the image is ready for subsampling
      <br>-----------</br>
    * 是否已经准备好了
* `subsampling.imageInfo: ImageInfo`。
    * The information of the image, including width, height, format, exif information, etc
      <br>-----------</br>
    * 图片的尺寸、格式信息
* `subsampling.exifOrientation: ExifOrientation`。
    * The exif information of the image
      <br>-----------</br>
    * 图片的 exif 信息
* `subsampling.foregroundTiles: List<TileSnapshot>`。
    * List of current foreground tiles
      <br>-----------</br>
    * 当前前景图块列表
* `subsampling.backgroundTiles: List<TileSnapshot>`。
    * List of current background tiles
      <br>-----------</br>
    * 当前背景图块列表
* `subsampling.sampleSize: Int`。
    * The sample size of the image
      <br>-----------</br>
    * 当前采样大小
* `subsampling.imageLoadRect: IntRect`。
    * The image load rect
      <br>-----------</br>
    * 原图上当前实际加载的区域
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`。
    * Tile grid size map
      <br>-----------</br>
    * 磁贴网格大小映射表

#### Listen property changed/监听属性变化

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
  <br>-----------</br>
  compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening
  <br>-----------</br>
  view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[TileBitmapPool]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/TileBitmapPool.kt

[TileBitmapCache]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/TileBitmapCache.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt