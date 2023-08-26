## Subsampling/子采样

> * The following example takes precedence over the Compose version of the ZoomImage component for
    demonstration
> * The API of ZoomImageView is exactly the same as ZoomImage, except that the entrance is different
> * ZoomState.zoomable is equivalent to ZoomImageView.zoomAbility
> * ZoomState.subsampling is equivalent to ZoomImageView.subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的 ZoomImage 组件来演示
> * ZoomImageView 的 API 和 ZoomImage 一模一样，只是入口不一样
> * ZoomState.zoomable 等价于 ZoomImageView.zoomAbility
> * ZoomState.subsampling 等价于 ZoomImageView.subsamplingAbility

有一些图片的尺寸巨大，如果把它们完整的读到内存肯定会让 App 因内存不足而崩溃

图片加载框架通常会采样后再加载，这时图片的尺寸会变小，但是图片的内容也会变的模糊不清

所以就需要 zoomimage 在缩放时能够支持子采样，用户滑动到哪里就对哪里进行子采样，然后将清晰的原图碎片显示到屏幕上，这样就能够在缩放时既显示清晰的图片，又不会让
App 崩溃

### 前置条件

什么情况下才会开启超大图采样功能？

1. contentSize 比 contentOriginSize 小
2. contentSize 的宽高比和 contentOriginSize 的宽高比相差不超过 0.5f
3. 图片是 BitmapRegionDecoder 支持的类型

### 使用子采样功能

集成了图片加载库的组件无需任何额外的工作即可使用子采样功能

ZoomImage 和 ZoomImageView 没有集成图片加载库的组件，需要额外调用 setImageSource() 方法以使用子采样功能，如下：

```kotlin
val state: ZoomState by rememberZoomState()

val imageSource by remember {
    mutableStateOf(ImageSource.fromResource(LocalContext.current, R.drawable.huge_image))
}
state.subsampling.setImageSource(imageSource)

ZoomImage(
    painter = painterResource(R.drawable.huge_image_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

view:

```kotlin
val zoomImageView: ZoomImageView = ...

val imageSource = ImageSource.fromResource(LocalContext.current, R.drawable.huge_image)
zoomImageView.subsamplingAbility.setImageSource(imageSource)

zoomImageView.setImageResource(R.drawable.huge_image_thumbnail)
```

### Exif Orientation

zoomimage 默认会读取图片的 Exif Orientation 信息，然后旋转图片，如果你不想让 zoomimage
读取 Exif Orientation 信息，可以修改 ignoreExifOrientation 参数为 true，如下：

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

### pause/resume

zoomimage 支持暂停和恢复子采样，暂停后会释放已加载的碎片并不再加载新碎片，如下：

```kotlin
val state: ZoomState by rememberZoomState()

// pause
state.subsampling.paused = true
// resume
state.subsampling.paused = false

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

#### Lifecycle

zoomimage 默认会监听 Lifecycle，在 Lifecycle stop 或 start 时暂停或恢复子采样

zoomimage 会自动获取最近的 Lifecycle，如果是在 Fragment 中就会获取到 Fragment 的
Lifecycle，无需主动设置，compose 和 view 都可以

背后依赖的是 compos 的 LocalLifecycleOwner API 和 Lifecycle KTX 包中的
View.findViewTreeLifecycleOwner() API

如果是在特殊的运行环境中，上述 API 无法获取到 Lifecycle，还可以手动设置 Lifecycle，如下：

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

子采样功能支持内存缓存，可以将 Bitmap 缓存在内存中，这样可以避免重复解码，提高性能

集成了图片加载库的组件无需任何额外的工作即可使用内存缓存功能，而没有集成图片加载库的组件需要先实现自己的
TileMemoryCache 然后设置 tileMemoryCache 属性才能使用内存缓存功能，如下：

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

设置了 tileMemoryCache 属性后就开启了内存缓存功能，还可以在不修改 tileMemoryCache 属性的情况下通过
disableMemoryCache 属性控制使用内存缓存功能，如下：

```kotlin
val state: ZoomState by rememberZoomState()

// 禁用内存缓存
state.subsampling.disableMemoryCache = true
// 允许使用内存缓存
state.subsampling.disableMemoryCache = false

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### Reuse Bitmap

子采样功能支持重用 Bitmap，可以使用已经存在的 Bitmap 解码新的碎片，这样可以避免重复创建
Bitmap，避免内存抖动，提高性能

集成了图片加载库的组件无需任何额外的工作即可使用重用 Bitmap 功能，而没有集成图片加载库的组件需要先实现自己的
TileBitmapPool 然后设置 tileBitmapPool 属性才能使用内存重用 Bitmap 功能，如下：

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

设置了 tileBitmapPool 属性后就开启了内存重用 Bitmap 功能，还可以在不修改 tileBitmapPool 属性的情况下通过
disallowReuseBitmap 属性控制重用 Bitmap，如下：

```kotlin
val state: ZoomState by rememberZoomState()

// 禁止重用 Bitmap
state.subsampling.disallowReuseBitmap = true
// 允许重用 Bitmap
state.subsampling.disallowReuseBitmap = false

ZoomImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### 监听相关事件

compose 版本的相关属性是用 State 包装的，直接读取它即可实现监听

view 版本的需要注册监听器，如下：

```kotlin
val zoomImageView: ZoomImageView = ...

zoomImageView.subsumplingAbility.registerOnTileChangedListener {
    // tileList 变化
}

zoomImageView.subsumplingAbility.registerOnReadyChangeListener {
    // ready 状态变化
}

zoomImageView.subsumplingAbility.registerOnPauseChangeListener {
    // paused 状态变化
}

zoomImageView.subsumplingAbility.registerOnImageLoadRectChangeListener {
    // imageLoadRect 变化
}
```

### 获取相关信息

* SubsamplingState.ready: Boolean。是否已经准备好了
* SubsamplingState.imageInfo: ImageInfo。当前碎片的快照信息列表
* SubsamplingState.tileList: List<TileSnapshot>。当前碎片的快照信息列表
* SubsamplingState.imageLoadRect: IntRect。原图上当前实际加载的区域