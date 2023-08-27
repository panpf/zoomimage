## 开始使用

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

### Components·组件

zoomimage 库包含了多个组件可供选择，你可以根据自己的需求选择合适的组件。

*不同的组件需要导入不同的依赖，请参考 [README] 导入对应的依赖*

compose：

* [SketchZoomAsyncImage]：集成了 [Sketch] 图片加载库的缩放 Image 组件`（推荐使用）`
    * 用法和 [Sketch] 的 [AsyncImage][SketchAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：集成了 [Coil] 图片加载库的缩放 Image 组件
    * 用法和 [Coil] 的 [AsyncImage][CoilAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：集成了 [Glide] 图片加载库的缩放 Image 组件
    * 用法和 [Glide] 的 [GlideImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomAsyncImageSample]
* [ZoomImage]：最基础的缩放 Image 组件，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageSample]

view：

* [SketchZoomImageView]：集成了 [Sketch] 图片加载库的缩放 ImageView`（推荐使用）`
    * 已适配 [Sketch] 支持子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomImageViewFragment]
* [CoilZoomImageView]：集成了 [Coil] 图片加载库的缩放 ImageView
    * 已适配 [Coil] 支持子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomImageViewFragment]
* [GlideZoomImageView]：集成了 [Glide] 图片加载库的缩放 ImageView
    * 已适配 [Glide] 支持子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：集成了 [Picasso] 图片加载库的缩放 ImageView
    * 已适配 [Picasso] 支持子采样，无需做任何额外的工作
    * 参考示例 [PicassoZoomImageViewFragment]
* [ZoomImageView]：最基础的缩放 ImageView，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageViewFragment]

总结：

* 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
* 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 setImageSource() 方法以支持子采样功能

### 使用

#### Compose

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
val sketchZoomImageView: SketchZoomImageView = ...
sketchZoomImageView.displayImage("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
    crossfade()
}

val coilZoomImageView: CoilZoomImageView = ...
coilZoomImageView.load("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
    crossfade(true)
}

val glideZoomImageView: GlideZoomImageView = ...
Glide.with(this@GlideZoomImageViewFragment)
    .load("http://sample.com/sample.jpg")
    .placeholder(R.drawable.placeholder)
    .into(glideZoomImageView)

val picassoZoomImageView: PicassoZoomImageView = ...
binding.picassoZoomImageViewImage.loadImage("http://sample.com/sample.jpg") {
    placeholder(R.drawable.placeholder)
}

val zoomImageView: ZoomImageView = ...
zoomImageView.setImageResource(R.drawable.huge_image_thumbnail)
val imageSource = ImageSource.fromResource(zoomImageView.context, R.drawable.huge_image)
zoomImageView.subsamplingAbility.setImageSource(imageSource)
```

> PicassoZoomImageView 为了监听加载结果以及获得 uri，无奈之下对官方 API 进行封装提供了一套专用的
> API，所以请不要直接使用官方的 API 去加载图片

zoom 和子采样的对外 API 封装在不同的类中，compose 版本是 ZoomableState 和 SubsamplingState，view 版本是
ZoomAbility 和 SubsamplingAbility，如下：

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


val sketchZoomImageView: SketchZoomImageView = ...
sketchZoomImageView.zoomAbility  // ZoomAbility
sketchZoomImageView.subsamplingAbility   // SubsamplingAbility
```

*更多缩放、偏移、旋转、子采样、阅读模式、滚动条等功能请参考 [Document](#Document·文档)*

### contentScale 和 alignment

zoomimage 支持所有的 [ContentScale] 和 [Alignment]

得益于 compose 版本和 view 版本使用的是同一套逻辑代码，ZoomImageView 在支持 ScaleType
之外也支持 [ContentScale] 和 [Alignment]，如下：

```kotlin
val sketchZoomImageView: SketchZoomImageView = ...

sketchZoomImageView.zoomAbility.contentScale = ContentScale.None
sketchZoomImageView.zoomAbility.alignment = Alignment.BottomEnd
```

### 获取相关信息

* ZoomableState.transform: Transform。获取当前的变换信息，包括缩放、偏移、旋转
* ZoomableState.baseTransform: Transform。获取当前的基础变换信息，包括缩放、偏移、旋转，受
  contentScale、alignment 以及 rotate() 方法影响
* ZoomableState.userTransform: Transform。获取当前的用户变换信息，包括缩放、偏移、旋转，受 scale()
  方法、location() 方法以及用户手势操作影响
* ZoomableState.minScale: Float。当前最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* ZoomableState.mediumScale: Float。当前中间缩放比例，用于双击缩放时的一个循环缩放比例
* ZoomableState.maxScale: Float。当前最大缩放比例，，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例
* ZoomableState.transforming: Boolean。当前是否正在变换中，包括缩放、偏移、旋转
* ZoomableState.contentBaseDisplayRect: IntRect。当前 content 在 container 中的基础显示区域，受
  contentScale、alignment 以及 rotate() 方法影响
* ZoomableState.contentBaseVisibleRect: IntRect。当前 content 的基础可见区域，受
  contentScale、alignment 以及 rotate() 方法影响
* ZoomableState.contentDisplayRect: IntRect。当前 content 在 container 中的显示区域，受
  contentScale、alignment 以及 scale()、rotate()、location() 以及以及用户手势操作的影响
* ZoomableState.contentVisibleRect: IntRect。当前 content 的可见区域，受
  contentScale、alignment 以及 scale()、rotate()、location() 以及以及用户手势操作的影响
* ZoomableState.scrollEdge: ScrollEdge。当前偏移状态的边界信息，例如是否到达左边界、右边界、上边界、下边界等
* ZoomableState.containerSize: IntSize。当前 container 的大小
* ZoomableState.contentSize: IntSize。当前 content 的大小
* ZoomableState.contentOriginSize: IntSize。当前 content 的原始大小

## Document·文档

* [Scale: scale, double-click scale, duration setting/缩放、双击缩放、时长设置](scale.md)
* [Offset: Move to the specified position/移动到指定位置](offset.md)
* [Location: Moves the specified location of the picture to the middle of the screen/将图片的指定位置移动到屏幕中间](location.md)
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