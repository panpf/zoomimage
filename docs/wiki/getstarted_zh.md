## Get Started/开始使用

翻译：[English](getstarted.md)

> * 以下示例优先用 Compose 版本的组件来演示
> * [ZoomState].zoomable 等价于 [ZoomImageView].zoomable
> * [ZoomState].subsampling 等价于 [ZoomImageView].subsampling

### 组件

ZoomImage 库包含了多个组件可供选择，你可以根据自己的需求选择合适的组件。

*不同的组件需要导入不同的依赖，请参考 [README](../../README_zh.md) 导入对应的依赖*

compose android：

* [SketchZoomAsyncImage]：`推荐使用`
    * 集成了 [Sketch] 图片加载库的缩放 Image 组件，用法和 [Sketch] 的 [AsyncImage][SketchAsyncImage]
      组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：
    * 集成了 [Coil] 图片加载库的缩放 Image 组件，用法和 [Coil] 的 [AsyncImage][CoilAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：
    * 集成了 [Glide] 图片加载库的缩放 Image 组件，用法和 [Glide] 的 [GlideImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomAsyncImageSample]

compose multiplatform：

* [ZoomImage]：
    * 最基础的缩放 Image 组件，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageSample]

view：

* [SketchZoomImageView]：`推荐使用`
    * 集成了 [Sketch] 图片加载库的缩放 ImageView
    * 已适配 [Sketch] 支持子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomImageViewFragment]
* [CoilZoomImageView]：
    * 集成了 [Coil] 图片加载库的缩放 ImageView
    * 已适配 [Coil] 支持子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomImageViewFragment]
* [GlideZoomImageView]：
    * 集成了 [Glide] 图片加载库的缩放 ImageView
    * 已适配 [Glide] 支持子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：
    * 集成了 [Picasso] 图片加载库的缩放 ImageView
    * 已适配 [Picasso] 支持子采样，无需做任何额外的工作
    * 参考示例 [PicassoZoomImageViewFragment]
* [ZoomImageView]：
    * 最基础的缩放 ImageView，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageViewFragment]

总结：

* 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
* 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 `subsampling.setImageSource(ImageSource)`
  方法以支持子采样功能

### 示例

#### compose android

```kotlin
SketchZoomAsyncImage(
    request = DisplayRequest(LocalContext.current, "http://sample.com/sample.jpg") {
        placeholder(R.drawable.placeholder)
        crossfade()
    },
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

CoilZoomAsyncImage(
    model = ImageRequest.Builder(LocalContext.current).apply {
        data("http://sample.com/sample.jpg")
        placeholder(R.drawable.placeholder)
        crossfade(true)
    }.build(),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

GlideZoomAsyncImage(
    model = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
) {
    it.placeholder(R.drawable.placeholder)
}
```

#### compose multiplatform

```kotlin
/* 
 * android
 */
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

/* 
 * desktop
 */
val state: ZoomState by rememberZoomState()
LaunchedEffect(Unit) {
    state.subsampling.setImageSource(ImageSource.fromResource("huge_image.jpeg"))
}
ZoomImage(
    painter = painterResource("huge_image_thumbnail.jpeg"),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

#### view:

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
    crossfade()
}

val coilZoomImageView = CoilZoomImageView(context)
coilZoomImageView.load("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
    crossfade(true)
}

val glideZoomImageView = GlideZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
    .load("http://sample.com/sample.jpg")
    .placeholder(R.drawable.placeholder)
    .into(glideZoomImageView)

val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageViewImage.loadImage("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
}

val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_image_thumbnail)
val imageSource = ImageSource.fromResource(context, R.drawable.huge_image)
zoomImageView.subsampling.setImageSource(imageSource)
```

> PicassoZoomImageView 提供了一组专用 API 来监听加载结果并获取 URI，以便支持子采样，因此请不要直接使用官方API
> 加载图片

zoom 和子采样的 API 封装在不同的类中，compose 版本是 [ZoomableState] 和 [SubsamplingState]，view
版本是 [ZoomableEngine] 和 [SubsamplingEngine]

示例：

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
val zoomable: ZoomableState = state.zoomable
val subsampling: SubsamplingState = state.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

*更多缩放、偏移、旋转、子采样、阅读模式、滚动条等功能详细介绍请参考页尾的文档*

### 可访问属性

```kotlin
// compose
val state: ZoomState by rememberZoomState()
SketchZoomAsyncImage(state = state)
val zoomable: ZoomableState = state.zoomable
val subsampling: SubsamplingState = state.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.baseTransform: Transform`: 基础变换信息，包括缩放、偏移、旋转，受 contentScale、alignment
  属性以及 rotate() 方法的影响
* `zoomable.userTransform: Transform`: 用户变换信息，包括缩放、偏移、旋转，受用户手势操作、readMode 属性以及
  scale()、offset()、locate()
  方法的影响
* `zoomable.transform: Transform`:
  最终的变换信息，包括缩放、偏移、旋转，等价于 `baseTransform + userTransform`
* `zoomable.minScale: Float`: 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.mediumScale: Float`: 中间缩放比例，用于双击缩放时的一个循环缩放比例
* `zoomable.maxScale: Float`: 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.continuousTransformType: Int`: 当前正在进行的连续变换的类型
* `zoomable.contentBaseDisplayRect: IntRect`: content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseVisibleRect: IntRect`: content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentDisplayRect: IntRect`: content 经过 transform 变换后在 container 中的区域
* `zoomable.contentVisibleRect: IntRect`: content 经过 transform 变换后自身对用户可见的区域
* `zoomable.scrollEdge: ScrollEdge`: 当前偏移的边界状态
* `zoomable.containerSize: IntSize`: 当前 container 的大小
* `zoomable.contentSize: IntSize`: 当前 content 的大小
* `zoomable.contentOriginSize: IntSize`: 当前 content 的原始大小
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

## 文档

* [Scale: 缩放、双击缩放、时长设置](scale_zh.md)
* [Offset: 移动到指定位置](offset_zh.md)
* [Locate: 定位到图片的任意位置并保持在屏幕中央](locate_zh.md)
* [Rotate: 旋转图片](rotate_zh.md)
* [Read Mode: 长图初始时充满屏幕，方便阅读](readmode_zh.md)
* [Click: 接收点击事件](click_zh.md)
* [Subsampling: 对超大图进行子采样显示，避免 OOM](subsampling_zh.md)
* [Scroll Bar: 显示水平和垂直滚动条，明确当前滚动位置](scrollbar_zh.md)
* [Log: 修改日志等级以及输出管道](log_zh.md)
* [Compose Multiplatform: 在桌面平台使用](multiplatform_zh.md)

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: ../../zoomimage-compose-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: ../../zoomimage-compose-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: ../../zoomimage-compose-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomAsyncImage.kt

[ZoomImageSample]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/ZoomImageSample.kt

[CoilZoomAsyncImageSample]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/CoilZoomAsyncImageSample.kt

[GlideZoomAsyncImageSample]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/SketchZoomAsyncImageSample.kt

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: ../../zoomimage-view-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: ../../zoomimage-view-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: ../../zoomimage-view-picasso/src/main/java/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: ../../zoomimage-view-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomImageView.kt

[ZoomImageViewFragment]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/ZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: ../../sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/SketchZoomImageViewFragment.kt

[Sketch]: https://github.com/panpf/sketch

[SketchAsyncImage]: https://github.com/panpf/sketch/blob/main/sketch-compose/src/main/java/com/github/panpf/sketch/compose/AsyncImage.kt

[Coil]: https://github.com/coil-kt/coil

[CoilAsyncImage]: https://github.com/coil-kt/coil/blob/main/coil-compose-singleton/src/main/java/coil/compose/SingletonAsyncImage.kt

[Glide]: https://github.com/bumptech/glide

[GlideImage]: https://github.com/bumptech/glide/blob/master/integration/compose/src/main/java/com/bumptech/glide/integration/compose/GlideImage.kt

[Picasso]: https://github.com/square/picasso

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[ZoomableEngine]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/zoom/ZoomableEngine.kt

[SubsamplingEngine]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/subsampling/SubsamplingEngine.kt

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt