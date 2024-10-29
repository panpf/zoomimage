## 子采样

翻译：[English](subsampling.md)

> [!TIP]
> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

有一些图片的尺寸巨大，如果把它们完整的读到内存会让 App
因内存不足而崩溃，图片加载框架通常会采样后再加载，这时图片的尺寸会变小，但是图片的内容也会变的模糊不清

所以就需要 ZoomImage 在缩放时能够支持子采样，用户滑动到哪里就对哪里进行子采样，然后将清晰的原图图块显示到屏幕上，
这样就能够在缩放时既显示清晰的图片，又不会让 App 崩溃

### 特点

* [Exif Orientation](#exif-orientation). 支持读取图片的 Exif Orientation 信息，然后旋转图片
* [动画](#图块动画). 在显示 Tile 的时候支持透明度动画，过渡更自然
* [背景图快](#背景图块). 切换 sampleSize 时图片清晰度逐级变化，过渡更自然
* [暂停加载图块](#暂停加载图块). 连续变换时暂停加载图块，提高性能
* [不可见时停止加载图块](#停止加载图块). 监听 Lifecycle，在 stop 时停止加载图块并释放已加载的图块，提高性能
* [内存缓存](#内存缓存). 避免重复解码，提高性能
* [可访问属性](#可访问属性). 可以读取采样大小、图片信息、图块列表等信息

### 前置条件

什么情况下才会开启子采样功能？

* contentSize 比 contentOriginSize 小
* contentSize 和 contentOriginSize 的边的缩放倍数相差不超过 1f
* Android 上必须是 BitmapRegionDecoder 支持的类型，非 Android 平台不是 GIF 就可以

### 使用子采样功能

集成了图片加载库的组件无需任何额外的工作即可使用子采样功能

[ZoomImage] 和 [ZoomImageView] 没有集成图片加载库，需要额外调用 `setImageSource(ImageSource)`
方法以使用子采样功能

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()
LaunchedEffect(zoomState.subsampling) {
    val resUri = Res.getUri("files/huge_world.jpeg")
    val imageSource = ImageSource.fromComposeResource(resUri)
    zoomState.setImageSource(imageSource)
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
zoomImageView.setImageSource(imageSource)
```

### ImageSource

[ImageSource] 负责为 ZoomImage 提供图片的数据用于解码，ZoomImage 提供了多种 [ImageSource]
的实现，以支持从各种来源加载图片，如下：

* [AssetImageSource]：从 Android 的 assets 目录加载图片。[ImageSource.fromAsset(context, "
  huge_world.jpeg")][AssetImageSource]
* [ByteArrayImageSource]：从 ByteArray
  加载图片。[ImageSource.fromByteArray(byteArray)][ByteArrayImageSource]
* [ComposeResourceImageSource]：从 Compose 的资源目录加载图片。[ImageSource.fromComposeResource(
  Res.getUri("files/huge_world.jpeg"))][ComposeResourceImageSource]
* [ContentImageSource]：从 Android 的 ContentProvider 加载图片。[ImageSource.fromContent(context,
  contentUri)][ContentImageSource]
* [FileImageSource]：从文件加载图片。[ImageSource.fromFile(file)][FileImageSource]
* [KotlinResourceImageSource]：从桌面或 ios 平台的 Kotlin
  资源目录加载图片。[ImageSource.fromKotlinResource("huge_world.jpeg")][KotlinResourceImageSource]
* [ResourceImageSource]：从 Android 的 res 目录加载图片。[ImageSource.fromResource(context,
  R.raw.huge_world)][ResourceImageSource]

### ModelToImageSource

Coil、Glide、Picasso 系列的组件在设置 ImageSource 时都需要将 model 或 data 转换为
ImageSource，ZoomImage 为他们提供了各自的 ModelToImageSource 和默认实现，如下：

* [CoilModelToImageSource][CoilModelToImageSource]：[CoilModelToImageSourceImpl][CoilModelToImageSourceImpl]
* [CoilModelToImageSource][CoilModelToImageSource2] for Coil
  2：[CoilModelToImageSourceImpl][CoilModelToImageSourceImpl2]  for Coil 2
* [GlideModelToImageSource]：[GlideModelToImageSourceImpl]
* [PicassoDataToImageSource]：[PicassoDataToImageSourceImpl]

如果默认实现无法正确的将 model 或 data 转换为 ImageSource 导致无法使用子采样，例如你自定义了 model 或
data，那么你必须自定义一个 ModelToImageSource 并应用它，如下：

```kotlin
/*
 * Coil
 */
class MyCoilModelToImageSource : CoilModelToImageSource {
    override suspend fun modelToImageSource(
        context: PlatformContext,
        imageLoader: ImageLoader,
        model: Any
    ): ImageSource.Factory? {
        // ...
    }
}

val coilModeToImageSources = remember { listOf(MyCoilModelToImageSource()).toImmutableList() }
val coilZoomState = rememberCoilZoomState(coilModeToImageSources)
CoilAsyncZoomImage(
    zoomState = coilZoomState,
    ...
)

val coilZoomImageView = CoilZoomImageView(context)
coilZoomImageView.registerModelToImageSource(MyCoilModelToImageSource())

/*
 * Glide
 */
class MyGlideModelToImageSource : GlideModelToImageSource {
    override suspend fun modelToImageSource(
        context: Context,
        imageLoader: Glide,
        model: Any
    ): ImageSource.Factory? {
        // ...
    }
}

val glideModeToImageSources = remember { listOf(MyGlideModelToImageSource()).toImmutableList() }
val glideZoomState = rememberGlideZoomState(glideModeToImageSources)
GlideAsyncZoomImage(
    zoomState = glideZoomState,
    ...
)

val glideZoomImageView = GlideZoomImageView(context)
glideZoomImageView.registerModelToImageSource(MyGlideModelToImageSource())

/*
 * Picasso
 */
class MyPicassoDataToImageSource : PicassoDataToImageSource {
    override suspend fun dataToImageSource(
        context: Context,
        picasso: Picasso,
        data: Any
    ): ImageSource.Factory? {
        // ...
    }
}

val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageView.registerDataToImageSource(MyPicassoDataToImageSource())
```

如果你自定义了 mode 或 data，那么你必需要自定义一个 ModelToImageSource 并应用它，否则将无法使用子采样功能

### Exif Orientation

ZoomImage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，你不能禁用它

### 图块动画

ZoomImage 在显示 Tile 的时候支持透明度动画，默认开启动画，持续时间 200 毫秒，刷新间隔 8 毫秒，你可以通过
`tileAnimationSpec` 参数来关闭动画或修改动画的持续时间和刷新间隔

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // 关闭动画
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec.None

    // 修改动画的持续时间和刷新间隔
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)
}

SketchZoomAsyncImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 暂停加载图块

ZoomImage 将连续变换行为分为 `SCALE`, `OFFSET`, `LOCATE`, `GESTURE`, `FLING`
五种类型，支持配置指定类型的连续变换暂停加载图块，这样可以提高性能

ZoomImage 在兼顾性能和体验的情况默认配置是 `SCALE`, `OFFSET`, `LOCATE`
三种类型的连续变换会暂停加载图块，`GESTURE`, `FLING` 两种类型会实时加载图块，
你可以通过 `pausedContinuousTransformTypes` 属性来配置它

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // 所有连续变换类型都实时加载图块
    zoomState.subsampling.pausedContinuousTransformTypes = 0

    // 所有连续变换类型都暂停加载图块
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

### 停止加载图块

ZoomImage 支持停止子采样，停止后会释放已加载的图块并不再加载新图块，重启后自动重新加载图块，
你可以通过 `stopped` 属性来配置它

示例：

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

ZoomImage 默认会自动获取最近的 Lifecycle 然后监听它的状态，在 Lifecycle stop 或 start 时停止或重启子采样

在 View 中通过 View.findViewTreeLifecycleOwner() API 获取到最近的 Lifecycle；在 Compose 通过
LocalLifecycleOwner.current API 获取 Lifecycle

### 背景图块

ZoomImage 通过背景图块实现了在切换 sampleSize 时随着 sampleSize
的变化图片清晰度也逐级变化的效果，并且在加载新图块的过程中也不会露出底图，这样就保证了清晰度变化的连续性，用户体验更好

但是此功能使用了更多的内存，在性能较差的设备上可能会对流畅性有影响，此功能默认开启，你可以通过 `disabledBackgroundTiles`
属性关闭它

示例：

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

### 内存缓存

子采样功能支持内存缓存，可以将 Bitmap 缓存在内存中，这样可以避免重复解码，提高性能

集成了图片加载库的组件无需任何额外的工作即可使用内存缓存功能，而没有集成图片加载库的组件需要先实现自己的
[TileImageCache] 然后设置 `tileImageCache` 属性才能使用内存缓存功能

示例：

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

设置了 tileImageCache 属性后就开启了内存缓存功能，还可以在不修改 tileImageCache 属性的情况下通过
`disabledTileImageCache` 属性控制使用内存缓存功能

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(zoomState.subsampling) {
    // 禁用内存缓存
  zoomState.subsampling.disabledTileImageCache = true
    // 允许使用内存缓存
  zoomState.subsampling.disabledTileImageCache = false
}

ZoomImage(
    imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 可访问属性

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
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `subsampling.ready: Boolean`: 是否已经准备好了
* `subsampling.imageInfo: ImageInfo`: 图片的尺寸、格式信息
* `subsampling.exifOrientation: ExifOrientation`: 图片的 exif 信息
* `subsampling.foregroundTiles: List<TileSnapshot>`: 当前前景图块列表
* `subsampling.backgroundTiles: List<TileSnapshot>`: 当前背景图块列表
* `subsampling.sampleSize: Int`: 当前采样大小
* `subsampling.imageLoadRect: IntRect`: 原图上当前实际加载的区域
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`: 磁贴网格大小映射表

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

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

[CoilModelToImageSource]: ../../zoomimage-core-coil3/src/commonMain/kotlin/com/github/panpf/zoomimage/coil/CoilModelToImageSource.kt

[CoilModelToImageSourceImpl]: ../../zoomimage-core-coil3/src/commonMain/kotlin/com/github/panpf/zoomimage/coil/CoilModelToImageSource.kt

[CoilModelToImageSource2]: ../../zoomimage-core-coil2/src/main/kotlin/com/github/panpf/zoomimage/coil/CoilModelToImageSource.kt

[CoilModelToImageSourceImpl2]: ../../zoomimage-core-coil2/src/main/kotlin/com/github/panpf/zoomimage/coil/CoilModelToImageSource.kt

[GlideModelToImageSource]: ../../zoomimage-core-glide/src/main/kotlin/com/github/panpf/zoomimage/glide/GlideModelToImageSource.kt

[GlideModelToImageSourceImpl]: ../../zoomimage-core-glide/src/main/kotlin/com/github/panpf/zoomimage/glide/GlideModelToImageSource.kt

[PicassoDataToImageSource]: ../../zoomimage-core-picasso/src/main/kotlin/com/github/panpf/zoomimage/picasso/PicassoDataToImageSource.kt

[PicassoDataToImageSourceImpl]: ../../zoomimage-core-picasso/src/main/kotlin/com/github/panpf/zoomimage/picasso/PicassoDataToImageSource.kt