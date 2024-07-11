## 子采样

翻译：[English](subsampling.md)

> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

有一些图片的尺寸巨大，如果把它们完整的读到内存肯定会让 App
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
* contentSize 的宽高比和 contentOriginSize 的宽高比相差不超过 0.5f
* Android 上必须是 BitmapRegionDecoder 支持的类型，桌面平台不是 GIF 就可以

### 使用子采样功能

集成了图片加载库的组件无需任何额外的工作即可使用子采样功能

[ZoomImage] 和 [ZoomImageView] 没有集成图片加载库，需要额外调用 `setImageSource(ImageSource)`
方法以使用子采样功能

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

val context = LocalContext.current
LaunchedEffect(Unit) {
    zoomState.subsampling.setImageSource(ImageSource.fromResource(context, R.drawable.huge_image))
}

ZoomImage(
    painter = painterResource(R.drawable.huge_image_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
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

ZoomImage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，你不能禁用它

### 图块动画

ZoomImage 在显示 Tile 的时候支持透明度动画，默认开启动画，持续时间 200 毫秒，刷新间隔 8 毫秒，你可以通过
`tileAnimationSpec` 参数来关闭动画或修改动画的持续时间和刷新间隔

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 关闭动画
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec.None

    // 修改动画的持续时间和刷新间隔
    zoomState.subsampling.tileAnimationSpec = TileAnimationSpec(duration = 400, interval = 16)
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
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
你可以通过 `pausedContinuousTransformType` 属性来配置它

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 所有连续变换类型都实时加载图块
    zoomState.subsampling.pausedContinuousTransformType = ContinuousTransformType.NONE

    // 所有连续变换类型都暂停加载图块
    zoomState.subsampling.pausedContinuousTransformType =
        TileManager.DefaultPausedContinuousTransformType or ContinuousTransformType.GESTURE or ContinuousTransformType.FLING
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
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

LaunchEffect(Unit) {
    // stop
    zoomState.subsampling.stopped = true
    // restart
    zoomState.subsampling.stopped = false
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
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

LaunchEffect(Unit) {
    zoomState.subsampling.disabledBackgroundTiles = true
}

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

### 内存缓存

子采样功能支持内存缓存，可以将 Bitmap 缓存在内存中，这样可以避免重复解码，提高性能

集成了图片加载库的组件无需任何额外的工作即可使用内存缓存功能，而没有集成图片加载库的组件需要先实现自己的
[TileBitmapCache] 然后设置 `tileBitmapCache` 属性才能使用内存缓存功能

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    zoomState.subsampling.tileBitmapCache = MyTileBitmapCache()
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)
```

设置了 tileBitmapCache 属性后就开启了内存缓存功能，还可以在不修改 tileBitmapCache 属性的情况下通过
`disabledTileBitmapCache` 属性控制使用内存缓存功能

示例：

```kotlin
val zoomState: ZoomState by rememberZoomState()

LaunchEffect(Unit) {
    // 禁用内存缓存
    zoomState.subsampling.disabledTileBitmapCache = true
    // 允许使用内存缓存
    zoomState.subsampling.disabledTileBitmapCache = false
}

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
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

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt

[TileBitmapCache]: ../../zoomimage-core/src/commonMain/kotlin/com/github/panpf/zoomimage/subsampling/TileBitmapCache.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt