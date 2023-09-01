## Subsampling/子采样

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

Some images are huge in size, if they are read completely into memory will definitely make the app
crash due to insufficient memory, the image loading framework will usually sample and then load,
then the size of the image will become smaller, but the content of the image will also become blurry
<br>-----------</br>
有一些图片的尺寸巨大，如果把它们完整的读到内存肯定会让 App
因内存不足而崩溃，图片加载框架通常会采样后再加载，这时图片的尺寸会变小，但是图片的内容也会变的模糊不清

Therefore, it is necessary that zoomimage can support subsampling when zooming, and the user can
subsampling wherever he slides, and then display the clear original image fragments on the screen,
so that it can display a clear picture when zooming without crashing the app
<br>-----------</br>
所以就需要 zoomimage 在缩放时能够支持子采样，用户滑动到哪里就对哪里进行子采样，然后将清晰的原图碎片显示到屏幕上，
这样就能够在缩放时既显示清晰的图片，又不会让 App 崩溃

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
    val imageSource = ImageSource.fromResource(context, R.drawable.huge_image)
    state.subsampling.setImageSource(imageSource)
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
zoomImageView.subsamplingAbility.setImageSource(imageSource)
```

### Exif Orientation

By default, zoomimage will read the Exif Orientation information of the image, and then rotate the
image, if you do not want zoomimage to read the Exif Orientation information, you can modify the
ignoreExifOrientation parameter to true
<br>-----------</br>
zoomimage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，如果你不想让 zoomimage 读取 Exif
Orientation 信息，可以修改 ignoreExifOrientation 参数为 true

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.subsampling.ignoreExifOrientation = true

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Exif Orientation

By default, zoomimage will read the Exif Orientation information of the image, and then rotate the
image, if you do not want zoomimage to read the Exif Orientation information, you can modify the
ignoreExifOrientation parameter to true
<br>-----------</br>
zoomimage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，如果你不想让 zoomimage 读取 Exif
Orientation 信息，可以修改 ignoreExifOrientation 参数为 true

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.subsampling.ignoreExifOrientation = true

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Tile Animation/碎片动画

By default, zoomimage will read the Exif Orientation information of the image, and then rotate the
image, if you do not want zoomimage to read the Exif Orientation information, you can modify the
ignoreExifOrientation parameter to true
<br>-----------</br>
zoomimage 在显示 Tile 的时候支持透明度动画，默认开启动画，持续时间 200 毫秒，刷新间隔 8 毫秒，你可以通过 tileAnimationSpec 参数来关闭动画或修改动画的持续时间和刷新间隔

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

// Turn off animations
// 关闭动画
state.subsampling.tileAnimationSpec = TileAnimationSpec.None

// Modify the duration and refresh interval of the animation
// 修改动画的持续时间和刷新间隔
state.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### pauseWhenTransforming/变换中暂停加载

ZoomImage supports pausing the loading of tiles during continuous transformations, such as gesture
zooming, animation, fling, etc.
This can avoid stuttering caused by frequent tile loading on devices with poor performance and
affect the smoothness of the animation, and automatically resume loading tiles after continuous
transformations, this feature is turned off by default, you can turn it on
<br>-----------</br>
ZoomImage 支持在连续变换时暂停加载磁贴，例如手势缩放中、动画中、fling 等，
这样可以在性能较差的设备上避免因频繁加载磁贴导致卡顿影响动画的流畅性，连续变换结束后自动恢复加载磁贴，此功能默认关闭，你可以开启它

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.subsampling.pauseWhenTransforming = true

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### stop/start

ZoomImage supports stopping subsampling, which free the loaded tile after stopping and no new tiles
are loaded, and automatically reloads the tiles after restarting
<br>-----------</br>
ZoomImage 支持停止子采样，停止后会释放已加载的磁贴并不再加载新磁贴，重启后自动重新加载磁贴

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

// stop
state.subsampling.stopped = true
// start
state.subsampling.stopped = false

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

#### Lifecycle

By default, zoomimage automatically fetches the most recent Lifecycle and listens to its state,
pausing or resuming subsampling at the Lifecycle stop or start
<br>-----------</br>
zoomimage 默认会自动获取最近的 Lifecycle 然后监听它的状态，在 Lifecycle stop 或 start 时停止或重启子采样

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
state.subsampling.setLifecycle(lifecycle)

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
[TileMemoryCache] Then set the tileMemoryCache property to use the memory cache feature
<br>-----------</br>
集成了图片加载库的组件无需任何额外的工作即可使用内存缓存功能，而没有集成图片加载库的组件需要先实现自己的
[TileMemoryCache] 然后设置 tileMemoryCache 属性才能使用内存缓存功能

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.subsampling.tileMemoryCache = MyTileMemoryCache()

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

After setting the tileMemoryCache property, the memory caching function is turned on, and it can be
passed without modifying the tileMemoryCache property
The disableMemoryCache property controls the use of the memory cache feature
<br>-----------</br>
设置了 tileMemoryCache 属性后就开启了内存缓存功能，还可以在不修改 tileMemoryCache 属性的情况下通过
disableMemoryCache 属性控制使用内存缓存功能

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

// Disable memory caching, 禁用内存缓存
state.subsampling.disableMemoryCache = true
// Memory caching is allowed, 允许使用内存缓存
state.subsampling.disableMemoryCache = false

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Reuse Bitmap

The subsampling feature supports the reuse of Bitmaps, and new fragments can be decoded using
existing Bitmaps, which reduces the creation of Bitmaps, reduces memory jitter, and improves
performance
<br>-----------</br>
子采样功能支持重用 Bitmap，可以使用已经存在的 Bitmap 解码新的碎片，这样可以减少创建 Bitmap，减少内存抖动，提高性能

Because only Sketch and Glide have BitmapPool, only components that integrate these two image
loading libraries can be reused without any additional work
Bitmap functionality, other components need to implement their own [TileBitmapPool] and then set the
tileBitmapPool property to use memory reuse
Bitmap functionality
<br>-----------</br>
因为只有 Sketch 和 Glide 有 BitmapPool，所以只有集成了这两个图片加载库的组件无需任何额外的工作即可使用重用
Bitmap 功能，其它组件需要先实现自己的 [TileBitmapPool] 然后设置 tileBitmapPool 属性才能使用内存重用
Bitmap 功能

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.subsampling.tileBitmapPool = MyTileBitmapPool()

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

After setting the tileBitmapPool property, the memory reuse Bitmap function is turned on, and it can
also be passed without modifying the tileBitmapPool property
The disallowReuseBitmap property controls the reuse of Bitmap
<br>-----------</br>
设置了 tileBitmapPool 属性后就开启了内存重用 Bitmap 功能，还可以在不修改 tileBitmapPool 属性的情况下通过
disallowReuseBitmap 属性控制重用 Bitmap

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

// Disabled reuse of Bitmaps，禁止重用 Bitmap
state.subsampling.disallowReuseBitmap = true
// Allows reuse of Bitmap，允许重用 Bitmap
state.subsampling.disallowReuseBitmap = false

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Listen for related events/监听相关事件

The relevant properties of the compose version are wrapped in State, which can be read directly to
achieve listening, and the view version needs to register a listener
<br>-----------</br>
compose 版本的相关属性是用 State 包装的，直接读取它即可实现监听，view 版本的则需要注册监听器

example/示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.subsumplingAbility.registerOnTileChangeListener {
    // tileSnapshotList changed
}

sketchZoomImageView.subsumplingAbility.registerOnReadyChangeListener {
    // ready changed
}

sketchZoomImageView.subsumplingAbility.registerOnStoppedChangeListener {
    // stopped changed
}

sketchZoomImageView.subsumplingAbility.registerOnImageLoadRectChangeListener {
    // imageLoadRect changed
}
```

### Get relevant information/获取相关信息

* [SubsamplingState].ready: Boolean。
    * Whether the image is ready for subsampling
      <br>-----------</br>
    * 是否已经准备好了
* [SubsamplingState].imageInfo: ImageInfo。
    * The information of the image, including width, height, format, exif information, etc
      <br>-----------</br>
    * 图片的尺寸、格式、exif 等信息
* [SubsamplingState].tileSnapshotList: List<TileSnapshot>。
    * A snapshot of the tile list
      <br>-----------</br>
    * 当前碎片的快照列表
* [SubsamplingState].imageLoadRect: IntRect。
    * The image load rect
      <br>-----------</br>
    * 原图上当前实际加载的区域

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt

[TileBitmapPool]: ../../zoomimage-core/src/main/java/com/github/panpf/zoomimage/subsampling/TileBitmapPool.kt

[TileMemoryCache]: ../../zoomimage-core/src/main/java/com/github/panpf/zoomimage/subsampling/TileMemoryCache.kt

[SubsamplingState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt