## Get Started/开始使用

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

### Components/组件

The ZoomImage library includes several components to choose from, so you can choose the right one
for your needs.
<br>-----------</br>
ZoomImage 库包含了多个组件可供选择，你可以根据自己的需求选择合适的组件。

*Different components need to import different dependencies, please refer to [README] to import the
corresponding dependencies*
<br>-----------</br>
*不同的组件需要导入不同的依赖，请参考 [README] 导入对应的依赖*

compose：

* [SketchZoomAsyncImage]：`recommended/推荐使用`
    * Zoom Image component integrated with [Sketch] image loading library, the usage is the same as
      the [AsyncImage] [SketchAsyncImage] component of [Sketch].
    * Network images and subsampling are already supported without any additional work
    * Reference Example [SketchZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Sketch] 图片加载库的缩放 Image 组件，用法和 [Sketch] 的 [AsyncImage][SketchAsyncImage]
      组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：
    * Zoom Image component integrated with the [Coil] image loading library, the usage is the same
      as the [AsyncImage][CoilAsyncImage] component of [Coil].
    * Network images and subsampling are already supported without any additional work
    * Reference example [CoilZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Coil] 图片加载库的缩放 Image 组件，用法和 [Coil] 的 [AsyncImage][CoilAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：
    * Zoom Image component that integrates the [Glide] image loading library, the usage is the same
      as the [GlideImage] component of [Glide].
    * Network images and subsampling are already supported without any additional work
    * Reference example [GlideZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Glide] 图片加载库的缩放 Image 组件，用法和 [Glide] 的 [GlideImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomAsyncImageSample]
* [ZoomImage]：
    * The most basic zoom Image component, not integrate the image loading library
    * Additional work needs to be done to support network pictures and subsampling
    * Reference example [ZoomImageSample]
      <br>-----------</br>
    * 最基础的缩放 Image 组件，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageSample]

view：

* [SketchZoomImageView]：`recommended/推荐使用`
    * Zoom ImageView with integrated [Sketch] image loading library
    * Adapted [Sketch] supports subsampling without any additional work
    * Reference example [SketchZoomImageViewFragment]
      <br>-----------</br>
    * 集成了 [Sketch] 图片加载库的缩放 ImageView
    * 已适配 [Sketch] 支持子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomImageViewFragment]
* [CoilZoomImageView]：
    * Zoomed ImageView with integrated [Coil] image loading library
    * Adapted [Coil] supports subsampling without any additional work
    * Reference example [CoilZoomImageViewFragment]
      <br>-----------</br>
    * 集成了 [Coil] 图片加载库的缩放 ImageView
    * 已适配 [Coil] 支持子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomImageViewFragment]
* [GlideZoomImageView]：
    * Zoomed ImageView with integrated [Glide] image loading library
    * Adapted [Glide] supports subsampling without any additional work
    * Reference example [GlideZoomImageViewFragment]
      <br>-----------</br>
    * 集成了 [Glide] 图片加载库的缩放 ImageView
    * 已适配 [Glide] 支持子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：
    * Zoomed ImageView integrated with the [Picasso] image loading library
    * Adapted [Picasso] supports subsampling without any additional work
    * Reference example [PicassoZoomImageViewFragment]
      <br>-----------</br>
    * 集成了 [Picasso] 图片加载库的缩放 ImageView
    * 已适配 [Picasso] 支持子采样，无需做任何额外的工作
    * 参考示例 [PicassoZoomImageViewFragment]
* [ZoomImageView]：
    * The most basic zoom ImageView, not integrating the image loading library
    * Additional work needs to be done to support network pictures and subsampling
    * Reference example [ZoomImageViewFragment]
      <br>-----------</br>
    * 最基础的缩放 ImageView，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageViewFragment]

Summary/总结：

* Components with integrated image loaders can support image and subsampling from any source without
  any additional work
* Components that do not integrate an image loader can only display local images and require an
  additional call to the setImageSource() method to support subsampling functionality
  <br>-----------</br>
* 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
* 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 setImageSource() 方法以支持子采样功能

### Example/示例

#### compose

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

// Relying on the alpha version of GlideImage is not recommended at this stage
// 依赖于 alpha 版本的 GlideImage，不推荐在现阶段使用
GlideZoomAsyncImage(
    model = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
) {
    it.placeholder(R.drawable.placeholder)
}

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
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.displayImage("http://sample.com/sample.jpg") {
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

> PicassoZoomImageView provides a set of specialized APIs to listen for load results and get URIs,
> to support subsampling
> so please don't load images directly using the official API
<br>-----------</br>
> PicassoZoomImageView 提供了一组专用 API 来监听加载结果并获取 URI，以便支持子采样，因此请不要直接使用官方
> API 加载图片

The APIs for zoom and subsampling are encapsulated in separate classes, and the compose versions
are [ZoomableState] and [SubsamplingState], view The versions are [ZoomableEngine]
and [SubsamplingEngine]
<br>-----------</br>
zoom 和子采样的 API 封装在不同的类中，compose 版本是 [ZoomableState] 和 [SubsamplingState]，view
版本是 [ZoomableEngine] 和 [SubsamplingEngine]

example/示例：

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

*For more detailed information about zoom, offset, rotation, subsampling, reading mode, scroll bar
and other functions, please refer to the documentation at the end of the page*
<br>-----------</br>
*更多缩放、偏移、旋转、子采样、阅读模式、滚动条等功能详细介绍请参考页尾的文档*

### contentScale 和 alignment

ZoomImage supports all [ContentScale] and [Alignment], because the compose version and the view
version use the same set of logic code, view The version of the component supports [ContentScale]
and [Alignment] in addition to [ScaleType]
<br>-----------</br>
ZoomImage 支持所有的 [ContentScale] 和 [Alignment]，得益于 compose 版本和 view 版本使用的是同一套逻辑代码，view
版本的组件在支持 [ScaleType] 之外也支持 [ContentScale] 和 [Alignment]

example/示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomable.contentScale = ContentScaleCompat.None
sketchZoomImageView.zoomable.alignment = AlignmentCompat.BottomEnd
```

### Listen for related events/监听相关事件

The relevant properties of the compose version are wrapped in State, which can be read directly to
achieve listening, and the view version needs to register a listener
<br>-----------</br>
compose 版本的相关属性是用 State 包装的，直接读取它即可实现监听，view 版本的则需要注册监听器

example/示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomable.registerOnTransformChangeListener {
    // transform changed
}

sketchZoomImageView.zoomable.registerOnResetListener {
    // reset
}

sketchZoomImageView.subsampling.registerOnTileChangeListener {
    // foregroundTiles, backgroundTiles changed
}

sketchZoomImageView.subsampling.registerOnReadyChangeListener {
    // ready changed
}

sketchZoomImageView.subsampling.registerOnStoppedChangeListener {
    // stopped changed
}

sketchZoomImageView.subsampling.registerOnImageLoadRectChangeListener {
    // imageLoadRect changed
}

sketchZoomImageView.onViewTapListener = { view: android.view.View, x: Float, y: Float ->
    // Click Events,单击事件
}

sketchZoomImageView.onViewLongPressListener = { view: android.view.View, x: Float, y: Float ->
    // Long press event,长按事件        
}
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
val zoomable: ZoomableState = state.zoomable
val subsampling: SubsamplingState = state.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> * Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
    suffixed with State compared to the compose version
> * 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

* `zoomable.baseTransform: Transform`。
    * Base transformation, include the base scale, offset, rotation, which is affected by
      contentScale, alignment properties and rotate method
      <br>-----------</br>
    * 基础变换信息，包括缩放、偏移、旋转，受 contentScale、alignment 属性以及 rotate() 方法的影响
* `zoomable.userTransform: Transform`。
    * User transformation, include the user scale, offset, rotation, which is affected by the user's
      gesture, readMode properties and scale, offset, locate method
      <br>-----------</br>
    * 用户变换信息，包括缩放、偏移、旋转，受用户手势操作、readMode 属性以及 scale()、offset()、locate()
      方法的影响
* `zoomable.transform: Transform`。
    * Final transformation, include the final scale, offset, rotation, is equivalent
      to `baseTransform + userTransform`
      <br>-----------</br>
    * 最终的变换信息，包括缩放、偏移、旋转，等价于 `baseTransform + userTransform`
* `zoomable.minScale: Float`。
    * Minimum scale factor, for limits the final scale factor, and as a target value for one of when
      switch scale
      <br>-----------</br>
    * 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.mediumScale: Float`。
    * Medium scale factor, only as a target value for one of when switch scale
      <br>-----------</br>
    * 中间缩放比例，用于双击缩放时的一个循环缩放比例
* `zoomable.maxScale: Float`。
    * Maximum scale factor, for limits the final scale factor, and as a target value for one of when
      switch scale
      <br>-----------</br>
    * 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.continuousTransformType: Int`。
    * If true, a transformation is currently in progress, possibly in a continuous gesture
      operation, or an animation is in progress
      <br>-----------</br>
    * 当前正在进行的连续变换的类型
* `zoomable.contentBaseDisplayRect: IntRect`。
    * The content region in the container after the baseTransform transformation
      <br>-----------</br>
    * content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseVisibleRect: IntRect`。
    * The content is visible region to the user after the baseTransform transformation
      <br>-----------</br>
    * content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentDisplayRect: IntRect`。
    * The content region in the container after the final transform transformation
      <br>-----------</br>
    * content 经过 transform 变换后在 container 中的区域
* `zoomable.contentVisibleRect: IntRect`。
    * The content is visible region to the user after the final transform transformation
      <br>-----------</br>
    * content 经过 transform 变换后自身对用户可见的区域
* `zoomable.scrollEdge: ScrollEdge`。
    * Edge state for the current offset
      <br>-----------</br>
    * 当前偏移的边界状态
* `zoomable.containerSize: IntSize`。
    * The size of the container that holds the content
      <br>-----------</br>
    * 当前 container 的大小
* `zoomable.contentSize: IntSize`。
    * The size of the content, usually Painter.intrinsicSize.round()
      <br>-----------</br>
    * 当前 content 的大小
* `zoomable.contentOriginSize: IntSize`。
    * The original size of the content
      <br>-----------</br>
    * 当前 content 的原始大小
* `subsampling.ready: Boolean`。
    * Whether the image is ready for subsampling
      <br>-----------</br>
    * 是否已经准备好了
* `subsampling.imageInfo: ImageInfo`。
    * The information of the image, including width, height, format, exif information, etc
      <br>-----------</br>
    * 图片的尺寸、格式、exif 等信息
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

## Document·文档

* [Scale: scale, double-click scale, duration setting/缩放、双击缩放、时长设置](scale.md)
* [Offset: Move to the specified position/移动到指定位置](offset.md)
* [Locate: Locate anywhere in the image and keeping it in the center of the screen/定位到图片的任意位置并保持在屏幕中央](locate.md)
* [Rotate: Rotate the image/旋转图片](rotate.md)
* [Read Mode: Automatically fills the screen for easy reading/自动充满屏幕，方便阅读](readmode.md)
* [Click: Receive click events/接收点击事件](click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM/对超大图进行子采样显示，避免 OOM](subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars to clarify the current scroll position/显示水平和垂直滚动条，明确当前滚动位置](scrollbar.md)
* [Log/日志](log.md)

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: ../../zoomimage-compose-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: ../../zoomimage-compose-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: ../../zoomimage-compose-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomAsyncImage.kt

[ZoomImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/ZoomImageSample.kt

[CoilZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/CoilZoomAsyncImageSample.kt

[GlideZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/SketchZoomAsyncImageSample.kt

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: ../../zoomimage-view-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: ../../zoomimage-view-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: ../../zoomimage-view-picasso/src/main/java/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: ../../zoomimage-view-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomImageView.kt

[ZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/ZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/SketchZoomImageViewFragment.kt

[Sketch]: https://github.com/panpf/sketch

[SketchAsyncImage]: https://github.com/panpf/sketch/blob/main/sketch-compose/src/main/java/com/github/panpf/sketch/compose/AsyncImage.kt

[Coil]: https://github.com/coil-kt/coil

[CoilAsyncImage]: https://github.com/coil-kt/coil/blob/main/coil-compose-singleton/src/main/java/coil/compose/SingletonAsyncImage.kt

[Glide]: https://github.com/bumptech/glide

[GlideImage]: https://github.com/bumptech/glide/blob/master/integration/compose/src/main/java/com/bumptech/glide/integration/compose/GlideImage.kt

[Picasso]: https://github.com/square/picasso

[README]: ../../README.md

[ContentScale]: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/ContentScale

[Alignment]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Alignment

[ScaleType]: https://developer.android.com/reference/android/widget/ImageView.ScaleType

[ZoomableState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[SubsamplingState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[ZoomableEngine]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/zoom/ZoomableEngine.kt

[SubsamplingEngine]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/subsampling/SubsamplingEngine.kt

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt