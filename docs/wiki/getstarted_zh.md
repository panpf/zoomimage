## Get Started/开始使用

翻译：[English](getstarted.md)

## 组件

ZoomImage 库包含了多个组件可供选择，你可以根据自己的需求选择合适的组件。

Compose multiplatform：

* [SketchZoomAsyncImage]：集成了 [Sketch]
  图片加载库，支持网络图片和子采样。示例：[SketchZoomAsyncImageSample]。`推荐使用`
* [CoilZoomAsyncImage]：集成了 [Coil]
  图片加载库，支持网络图片和子采样。示例：[CoilZoomAsyncImageSample]
* [ZoomImage]
  ：基础的缩放组件，未集成图片加载库，不支持网络图片，需要调用 `subsampling.setImageResource()`
  方法以支持子采样。示例：[ZoomImageSample]

Only android compose：

* [GlideZoomAsyncImage]：集成了 [Glide]
  图片加载库，支持网络图片和子采样。示例：[GlideZoomAsyncImageSample]

Android view：

* [SketchZoomImageView]：集成了 [Sketch]
  图片加载库，支持网络图片和子采样。示例：[SketchZoomImageViewFragment]。`推荐使用`
* [CoilZoomImageView]：集成了 [Coil]
  图片加载库，支持网络图片和子采样。示例：[CoilZoomImageViewFragment]
* [GlideZoomImageView]：集成了 [Glide]
  图片加载库，支持网络图片和子采样。示例：[GlideZoomImageViewFragment]
* [PicassoZoomImageView]：集成了 [Picasso]
  图片加载库，支持网络图片和子采样。示例：[PicassoZoomImageViewFragment]
* [ZoomImageView]
  ：基础的缩放组件，未集成图片加载库，不支持网络图片，需要调用 `subsampling.setImageResource()`
  方法以支持子采样。示例：[ZoomImageViewFragment]

> [!TIP]
> * 不同的组件需要导入不同的依赖，请参考 [README](../../README_zh.md#下载) 导入对应的依赖
> * 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
> * 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 `setSubsamplingImage()` 方法以支持子采样功能

## 示例

Compose multiplatform：

```kotlin
// 使用基础的 ZoomImage 组件
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

// 使用 SketchZoomAsyncImage 组件
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

// 使用 CoilZoomAsyncImage 组件
CoilZoomAsyncImage(
    model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

Only android compose：

```kotlin
// 使用 GlideZoomAsyncImage 组件
GlideZoomAsyncImage(
  model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

Android view：

```kotlin
// 使用基础的 ZoomImageImage 组件
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_world_thumbnail)
val imageSource = ImageSource.fromResource(context, R.raw.huge_world)
zoomImageView.setSubsamplingImage(imageSource)

// 使用 SketchZoomImageView 组件
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("https://sample.com/sample.jpeg")

// 使用 CoilZoomImageView 组件
val coilZoomImageView = CoilZoomImageView(context)
coilZoomImageView.load("https://sample.com/sample.jpeg")

// 使用 GlideZoomImageView 组件
val glideZoomImageView = GlideZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
  .load("https://sample.com/sample.jpeg")
    .into(glideZoomImageView)

// 使用 PicassoZoomImageView 组件
val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageViewImage.loadImage("https://sample.com/sample.jpeg")
```

> [!TIP]
> * PicassoZoomImageView 提供了一组专用 API 来监听加载结果并获取 URI，以便支持子采样，因此请不要直接使用官方API
    加载图片
> * 各个组件图片加载相关的更多使用方法请参考其原本组件的用法

## 缩放和子采样

缩放和子采样的 API 封装在不同的类中，你可以直接通过它们去控制缩放和子采样或获取相关信息，如下：

* compose 版本是 [ZoomableState] 和 [SubsamplingState]
* view 版本是 [ZoomableEngine] 和 [SubsamplingEngine]

示例：

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(
  imageUri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
  zoomState = zoomState,
)
val zoomable: ZoomableState = zoomState.zoomable
val subsampling: SubsamplingState = zoomState.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

### 可访问属性

> [!TIP] 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀

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

### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

> [!TIP]
> 更多缩放、偏移、旋转、子采样、阅读模式、滚动条等功能详细介绍请参考页尾的文档链接

## 文档

* [Scale: 缩放图片以查看更清晰的细节](scale_zh.md)
* [Offset: 移动图片以查看容器之外的内容](offset_zh.md)
* [Rotate: 旋转图片以不同角度查看内容](rotate_zh.md)
* [Locate: 定位到图片的任意](locate_zh.md)
* [Read Mode: 长图初始时充满屏幕方便阅读](readmode_zh.md)
* [Click: 接收点击事件](click_zh.md)
* [Subsampling: 通过子采样的方式显示大图避免 OOM](subsampling_zh.md)
* [Scroll Bar: 显示水平和垂直滚动条](scrollbar_zh.md)
* [Log: 修改日志等级以及输出管道](log_zh.md)
* [Modifier.zoom()](modifier_zoom_zh.md)

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: ../../zoomimage-compose-coil3/src/commonMain/kotlin/com/github/panpf/zoomimage/SingletonCoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: ../../zoomimage-compose-glide/src/main/kotlin/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: ../../zoomimage-compose-sketch4/src/commonMain/kotlin/com/github/panpf/zoomimage/SingletonSketchZoomAsyncImage.kt

[ZoomImageSample]: ../../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/BasicZoomImageSample.kt

[CoilZoomAsyncImageSample]: ../../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/CoilZoomAsyncImageSample.common.kt

[GlideZoomAsyncImageSample]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: ../../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/SketchZoomAsyncImageSample.kt

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: ../../zoomimage-view-coil3-core/src/main/kotlin/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: ../../zoomimage-view-glide/src/main/kotlin/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: ../../zoomimage-view-picasso/src/main/kotlin/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: ../../zoomimage-view-sketch4-core/src/main/kotlin/com/github/panpf/zoomimage/SketchZoomImageView.kt

[ZoomImageViewFragment]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/BasicZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: ../../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/SketchZoomImageViewFragment.kt

[Sketch]: https://github.com/panpf/sketch

[SketchAsyncImage]: https://github.com/panpf/sketch/blob/main/sketch-compose/src/commonMain/kotlin/com/github/panpf/sketch/compose/AsyncImage.kt

[Coil]: https://github.com/coil-kt/coil

[CoilAsyncImage]: https://github.com/coil-kt/coil/blob/main/coil-compose-singleton/src/commonMain/kotlin/coil/compose/SingletonAsyncImage.kt

[Glide]: https://github.com/bumptech/glide

[GlideImage]: https://github.com/bumptech/glide/blob/master/integration/compose/src/commonMain/kotlin/com/bumptech/glide/integration/compose/GlideImage.kt

[Picasso]: https://github.com/square/picasso

[ZoomableState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[SubsamplingState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[ZoomableEngine]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/view/zoom/ZoomableEngine.kt

[SubsamplingEngine]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/view/subsampling/SubsamplingEngine.kt

[ZoomImageView]: ../../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt