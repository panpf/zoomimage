## Get Started/开始使用

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

* [SketchZoomAsyncImage]：
    * Zoom Image component integrated with [Sketch] image loading library `(recommended)`
    * The usage is the same as the [AsyncImage] [SketchAsyncImage] component of [Sketch].
    * Network images and subsampling are already supported without any additional work
    * Reference Example [SketchZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Sketch] 图片加载库的缩放 Image 组件`（推荐使用）`
    * 用法和 [Sketch] 的 [AsyncImage][SketchAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：
    * Zoom Image component integrated with the [Coil] image loading library
    * The usage is the same as the [AsyncImage][CoilAsyncImage] component of [Coil].
    * Network images and subsampling are already supported without any additional work
    * Reference example [CoilZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Coil] 图片加载库的缩放 Image 组件
    * 用法和 [Coil] 的 [AsyncImage][CoilAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：
    * Zoom Image component that integrates the [Glide] image loading library
    * The usage is the same as the [GlideImage] component of [Glide].
    * Network images and subsampling are already supported without any additional work
    * Reference example [GlideZoomAsyncImageSample]
      <br>-----------</br>
    * 集成了 [Glide] 图片加载库的缩放 Image 组件
    * 用法和 [Glide] 的 [GlideImage] 组件一样
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

* [SketchZoomImageView]：
    * Zoom ImageView with integrated [Sketch] image loading library `(recommended)`
    * Adapted [Sketch] supports subsampling without any additional work
    * Reference example [SketchZoomImageViewFragment]
      <br>-----------</br>
    * 集成了 [Sketch] 图片加载库的缩放 ImageView`（推荐使用）`
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
zoomImageView.subsamplingAbility.setImageSource(imageSource)
```

> PicassoZoomImageView provides a set of specialized APIs to listen for load results and get URIs,
> so please don't load images directly using the official API
<br>-----------</br>
> PicassoZoomImageView 提供了一组专用 API 来监听加载结果并获取 URI，因此请不要直接使用官方 API 加载图片

The APIs for zoom and subsampling are encapsulated in separate classes, and the compose versions
are [ZoomableState] and [SubsamplingState], view The versions are [ZoomAbility]
and [SubsamplingAbility]
<br>-----------</br>
zoom 和子采样的 API 封装在不同的类中，compose 版本是 [ZoomableState] 和 [SubsamplingState]，view
版本是 [ZoomAbility] 和 [SubsamplingAbility]

example/示例：

```kotlin
val state: ZoomState by rememberZoomState()

state.zoomable  // ZoomableState
state.subsampling   // SubsamplingState

SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)


val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.zoomAbility  // ZoomAbility
sketchZoomImageView.subsamplingAbility   // SubsamplingAbility
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

sketchZoomImageView.zoomAbility.contentScale = ContentScaleCompat.None
sketchZoomImageView.zoomAbility.alignment = AlignmentCompat.BottomEnd
```

### Listen for related events/监听相关事件

The relevant properties of the compose version are wrapped in State, which can be read directly to
achieve listening, and the view version needs to register a listener
<br>-----------</br>
compose 版本的相关属性是用 State 包装的，直接读取它即可实现监听，view 版本的则需要注册监听器

example/示例：

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)

sketchZoomImageView.zoomAbility.registerOnTransformChangeListener {
    // transform changed
}

sketchZoomImageView.zoomAbility.registerOnResetListener {
    // reset
}

sketchZoomImageView.zoomAbility.registerOnViewTapListener { view: android.view.View, x: Float, y: Float ->
    // Click Events,单击事件
}

sketchZoomImageView.zoomAbility.registerOnViewLongPressListener { view: android.view.View, x: Float, y: Float ->
    // Long press event,长按事件        
}

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

* [ZoomableState].baseTransform: Transform。
    * Base transformation, include the base scale, offset, rotation, which is affected by contentScale, alignment properties and rotate method
      <br>-----------</br>
    * 基础变换信息，包括缩放、偏移、旋转，受 contentScale、alignment 属性以及 rotate() 方法的影响
* [ZoomableState].userTransform: Transform。
    * User transformation, include the user scale, offset, rotation, which is affected by the user's gesture, readMode properties and scale, offset, locate method
      <br>-----------</br>
    * 用户变换信息，包括缩放、偏移、旋转，受用户手势操作、readMode 属性以及 scale()、offset()、locate()
      方法的影响
* [ZoomableState].transform: Transform。
    * Final transformation, include the final scale, offset, rotation, is equivalent to `baseTransform + userTransform`
      <br>-----------</br>
    * 最终的变换信息，包括缩放、偏移、旋转，等价于 `baseTransform + userTransform`
* [ZoomableState].minScale: Float。
    * Minimum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
      <br>-----------</br>
    * 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* [ZoomableState].mediumScale: Float。
    * Medium scale factor, only as a target value for one of when switch scale
      <br>-----------</br>
    * 中间缩放比例，用于双击缩放时的一个循环缩放比例
* [ZoomableState].maxScale: Float。
    * Maximum scale factor, for limits the final scale factor, and as a target value for one of when switch scale
      <br>-----------</br>
    * 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例
* [ZoomableState].transforming: Boolean。
    * If true, a transformation is currently in progress, possibly in a continuous gesture operation, or an animation is in progress
      <br>-----------</br>
    * 是否正在变换中，可能是在连续的手势操作中或者正在执行动画
* [ZoomableState].contentBaseDisplayRect: IntRect。
    * The content region in the container after the baseTransform transformation
      <br>-----------</br>
    * content 经过 baseTransform 变换后在 container 中的区域
* [ZoomableState].contentBaseVisibleRect:
    * The content is visible region to the user after the baseTransform transformation
      <br>-----------</br>
    * content 经过 baseTransform 变换后自身对用户可见的区域
* [ZoomableState].contentDisplayRect: IntRect。
    * The content region in the container after the final transform transformation
      <br>-----------</br>
    * content 经过 transform 变换后在 container 中的区域
* [ZoomableState].contentVisibleRect: IntRect。
    * The content is visible region to the user after the final transform transformation
      <br>-----------</br>
    * content 经过 transform 变换后自身对用户可见的区域
* [ZoomableState].scrollEdge: ScrollEdge。
    * Edge state for the current offset
      <br>-----------</br>
    * 当前偏移的边界状态
* [ZoomableState].containerSize: IntSize。
    * The size of the container that holds the content
      <br>-----------</br>
    * 当前 container 的大小
* [ZoomableState].contentSize: IntSize。
    * The size of the content, usually Painter.intrinsicSize.round()
      <br>-----------</br>
    * 当前 content 的大小
* [ZoomableState].contentOriginSize: IntSize。
    * The original size of the content
      <br>-----------</br>
    * 当前 content 的原始大小
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
    * 当前图块的快照列表
* [SubsamplingState].imageLoadRect: IntRect。
    * The image load rect
      <br>-----------</br>
    * 原图上当前实际加载的区域

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

[ZoomAbility]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/zoom/ZoomAbility.kt

[SubsamplingAbility]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/view/subsampling/SubsamplingAbility.kt

[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/compose/ZoomState.kt