## Get Started/开始使用

翻译：[English](getting_started.md)

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
> * 不同的组件需要导入不同的依赖，请参考 [README](../README.zh.md#下载) 导入对应的依赖
> * 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
> * 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 `setSubsamplingImage()` 方法以支持子采样功能

## 示例

Compose multiplatform：

```kotlin
// 使用基础的 ZoomImage 组件
val zoomState: ZoomState by rememberZoomState()
val imageSource = remember {
    val resUri = Res.getUri("files/huge_world.jpeg")
    ImageSource.fromComposeResource(resUri)
}
zoomState.setSubsamplingImage(imageSource)
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
val zoomState: ZoomState by rememberSketchZoomState()
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
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

### 可访问属性和方法

> [!TIP] 注意：view 版本的相关属性用 StateFlow 包装，所以其名字相比 compose 版本都以 State 为后缀
> 可以读取也可以设置的属性:

只读属性：

* `zoomable.containerSize: IntSize`: 当前 container 的大小
* `zoomable.contentSize: IntSize`: 当前 content 的大小
* `zoomable.contentOriginSize: IntSize`: 当前 content 的原始大小
* `zoomable.contentScale: ContentScale`: content 的缩放方式，默认是 ContentScale.Fit
* `zoomable.alignment: Alignment`: content 在 container 中的对齐方式，默认是 Alignment.TopStart
* `zoomable.layoutDirection: LayoutDirection`: container 的布局方向，默认是 LayoutDirection.Ltr
* `zoomable.readMode: ReadMode?`: 阅读模式配置，默认是 null
* `zoomable.scalesCalculator: ScalesCalculator`: minScale、mediumScale 和 maxScale 计算器，默认是
  ScalesCalculator.Dynamic
* `zoomable.threeStepScale: Boolean`: 双击缩放时是否在 minScale、mediumScale 和 maxScale 之间循环缩放，默认是
  false
* `zoomable.rubberBandScale: Boolean`: 缩放超出 minScale 或 maxScale 后是否使用橡皮筋效果，默认是
  true
* `zoomable.oneFingerScaleSpec: OneFingerScaleSpec`: 单指缩放配置，默认是 OneFingerScaleSpec.Default
* `zoomable.animationSpec: ZoomAnimationSpec`: 缩放、偏移等动画配置，默认是 ZoomAnimationSpec.Default
* `zoomable.limitOffsetWithinBaseVisibleRect: Boolean`: 是否将偏移限制在 contentBaseVisibleRect
  内，默认是 false
* `zoomable.containerWhitespaceMultiple: Float`: 基于容器尺寸的倍数为容器四周添加空白区域，默认是 0f
* `zoomable.containerWhitespace: ContainerWhitespace`: 容器四周空白区域的配置，优先级高于
  containerWhitespaceMultiple，默认是 ContainerWhitespace.Zero
* `zoomable.keepTransformWhenSameAspectRatioContentSizeChanged: Boolean`: 是否在相同宽高比的
  contentSize 改变时保持 transform 不变，默认是 false
* `zoomable.disabledGestureTypes: Int`: 配置禁用的手势类型，默认是 0（不禁用任何手势），可以使用
  GestureType 的位或操作来组合多个手势类型
* `zoomable.reverseMouseWheelScale: Boolean`: 是否反转鼠标滚轮的方向，默认是 false
* `zoomable.mouseWheelScaleCalculator: MouseWheelScaleCalculator`: 鼠标滚轮缩放计算器，默认是
  MouseWheelScaleCalculator.Default
* `zoomable.transform: Transform`: 当前变换状态（baseTransform + userTransform）
* `zoomable.baseTransform: Transform`: 当前基础变换状态，受 contentScale 和 alignment 参数影响
* `zoomable.userTransform: Transform`: 当前用户变换状态，受 scale()、locate() 以及用户手势缩放、双击等操作影响
* `zoomable.minScale: Float`: 最小缩放比例，用于缩放时限制最小缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.mediumScale: Float`: 中间缩放比例，用于双击缩放时的一个循环缩放比例
* `zoomable.maxScale: Float`: 最大缩放比例，用于缩放时限制最大缩放比例以及双击缩放时的一个循环缩放比例
* `zoomable.continuousTransformType: Int`: 当前正在进行的连续变换的类型
* `zoomable.contentBaseDisplayRectF: Rect`: content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseDisplayRect: IntRect`: content 经过 baseTransform 变换后在 container 中的区域
* `zoomable.contentBaseVisibleRectF: Rect`: content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentBaseVisibleRect: IntRect`: content 经过 baseTransform 变换后自身对用户可见的区域
* `zoomable.contentDisplayRectF: Rect`: content 经过 transform 变换后在 container 中的区域
* `zoomable.contentDisplayRect: IntRect`: content 经过 transform 变换后在 container 中的区域
* `zoomable.contentVisibleRectF: Rect`: content 经过 transform 变换后自身对用户可见的区域
* `zoomable.contentVisibleRect: IntRect`: content 经过 transform 变换后自身对用户可见的区域
* `zoomable.sourceScaleFactor: ScaleFactor`: 以原图为基准的缩放比例
* `zoomable.sourceVisibleRectF: Rect`: contentVisibleRect 映射到原图上的区域
* `zoomable.sourceVisibleRect: IntRect`: contentVisibleRect 映射到原图上的区域
* `zoomable.scrollEdge: ScrollEdge`: 当前偏移的边界状态

* `subsampling.disabled: Boolean`: 是否禁用子采样功能
* `subsampling.tileImageCache: TileImageCache?`: Tile 图块的内存缓存，默认为 null
* `subsampling.disabledTileImageCache: Boolean`: 是否禁用 Tile 图块的内存缓存，默认为 false
* `subsampling.tileAnimationSpec: TileAnimationSpec`: 图块动画配置，默认为 TileAnimationSpec.Default
* `subsampling.pausedContinuousTransformTypes: Int`: 暂停加载图块的连续变换类型的配置
* `subsampling.disabledBackgroundTiles: Boolean`: 是否禁用背景图块，默认为 false
* `subsampling.stopped: Boolean`: 是否停止加载图块，默认为 false
* `subsampling.disabledAutoStopWithLifecycle: Boolean`: 是否禁用根据 Lifecycle 自动停止加载图块，默认为
  false
* `subsampling.regionDecoders: List<RegionDecoder.Factory>`: 添加自定义的 RegionDecoder，默认为空列表
* `subsampling.showTileBounds: Boolean`: 是否显示 Tile 的边界，默认为 false
* `subsampling.ready: Boolean`: 是否已经准备好了
* `subsampling.imageInfo: ImageInfo`: 图片的尺寸、格式信息
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`: 磁贴网格大小映射表
* `subsampling.sampleSize: Int`: 当前采样大小
* `subsampling.imageLoadRect: IntRect`: 原图上当前实际加载的区域
* `subsampling.foregroundTiles: List<TileSnapshot>`: 当前前景图块列表
* `subsampling.backgroundTiles: List<TileSnapshot>`: 当前背景图块列表

交互方法：

* `zoomable.setReadMode(ReadMode?)`: 设置阅读模式配置
* `zoomable.setScalesCalculator(ScalesCalculator)`: 设置 minScale、mediumScale 和 maxScale 的计算器
* `zoomable.setThreeStepScale(Boolean)`: 设置双击缩放时是否在 minScale、mediumScale 和 maxScale
  之间循环缩放
* `zoomable.setRubberBandScale(Boolean)`: 设置缩放超出 minScale 或 maxScale 后是否使用橡皮筋效果
* `zoomable.setOneFingerScaleSpec(OneFingerScaleSpec)`: 设置单指缩放配置
* `zoomable.setAnimationSpec(ZoomAnimationSpec)`: 设置缩放、偏移等动画配置
* `zoomable.setLimitOffsetWithinBaseVisibleRect(Boolean)`: 设置是否将偏移限制在
  contentBaseVisibleRect 内
* `zoomable.setContainerWhitespaceMultiple(Float)`: 设置基于容器尺寸的倍数为容器四周添加空白区域
* `zoomable.setContainerWhitespace(ContainerWhitespace)`: 设置容器四周空白区域的配置，优先级高于
  containerWhitespaceMultiple
* `zoomable.setKeepTransformWhenSameAspectRatioContentSizeChanged(Boolean)`: 设置是否在相同宽高比的
  contentSize 改变时保持 transform 不变
* `zoomable.setDisabledGestureTypes(Int)`: 设置禁用的手势类型，可以使用 GestureType 的位或操作来组合多个手势类型
* `zoomable.setReverseMouseWheelScale(Boolean)`: 设置是否反转鼠标滚轮的方向
* `zoomable.setMouseWheelScaleCalculator(MouseWheelScaleCalculator)`: 设置鼠标滚轮缩放计算器
* `zoomable.scale()`: 缩放 content 到指定的倍数
* `zoomable.scaleBy()`: 以乘法的方式增量缩放 content 指定的倍数
* `zoomable.scaleByPlus()`: 以加法的方式增量缩放 content 指定的倍数
* `zoomable.switchScale()`: 切换 content 的缩放倍数，默认在 minScale 和 mediumScale 之间循环， 如果
  threeStepScale 为 true 则在 minScale、mediumScale 和 maxScale 之间循环
* `zoomable.offset()`: 偏移 content 到指定的位置
* `zoomable.offsetBy()`: 以增量的方式偏移 content 指定的偏移量
* `zoomable.locate()`: 定位到 content 上的指定位置，也可以用时缩放到指定倍数
* `zoomable.rotate()`: 旋转 content 到指定的角度，角度只能是 90 的倍数
* `zoomable.rotateBy()`: 以增量的方式旋转 content 指定的角度，角度只能是 90 的倍数
* `zoomable.getNextStepScale(): Float`: 获取下一个缩放倍数，默认在 minScale 和 mediumScale 之间循环，
  如果 threeStepScale 为 true 则在 minScale、mediumScale 和 maxScale 之间循环
* `zoomable.touchPointToContentPoint(): IntOffset`: 将触摸点转换为 content 上的点，原点是 content
  的左上角
* `zoomable.touchPointToContentPointF(): Offset`: 将触摸点转换为 content 上的点，原点是 content 的左上角
* `zoomable.sourceToDraw(Offset): Offset`: 将原图上的点转换为绘制时的点，原点是 container 的左上角
* `zoomable.sourceToDraw(Rect): Rect`: 将原图上的矩形转换为绘制时的矩形，原点是 container 的左上角
* `zoomable.canScroll(): Boolean`: 判断当前 content 在指定方向上是否可以滚动

* `subsampling.setImage(SubsamplingImage?): Boolean`: 设置子采样图片，返回是否成功，集成图片加载器的组件会自动设置子采样图片
* `subsampling.setDisabled(Boolean)`: 设置是否禁用子采样功能
* `subsampling.setTileImageCache(TileImageCache?)`: 设置 Tile 图块的内存缓存，集成图片加载器的组件会自动设置它
* `subsampling.setDisabledTileImageCache(Boolean)`: 设置是否禁用 Tile 图块的内存缓存
* `subsampling.setTileAnimationSpec(TileAnimationSpec)`: 设置图块动画配置
* `subsampling.setPausedContinuousTransformTypes(Int)`: 设置暂停加载图块的连续变换类型的配置，可以通过位或运算符组合多个类型，默认为
  TileManager.DefaultPausedContinuousTransformType
* `subsampling.setDisabledBackgroundTiles(Boolean)`: 设置是否禁用背景图块
* `subsampling.setStopped(Boolean)`: 设置是否停止加载图块
* `subsampling.setDisabledAutoStopWithLifecycle(Boolean)`: 设置是否禁用根据 Lifecycle 自动停止加载图块功能
* `subsampling.setRegionDecoders(List<RegionDecoder.Factory>)`: 设置自定义的 RegionDecoder
* `subsampling.setShowTileBounds(Boolean)`: 设置是否显示 Tile 的边界

#### 监听属性变化

* compose 版本的相关属性是用 State 包装的，在 Composable 函数中直接读取它即可实现监听
* view 的相关属性是用 StateFlow 包装，调用其 collect 函数即可实现监听

> [!TIP]
> 更多缩放、偏移、旋转、子采样、阅读模式、滚动条等功能详细介绍请参考页尾的文档链接

## 文档

* [Scale: 缩放图片以查看更清晰的细节](scale.zh.md)
* [Offset: 移动图片以查看容器之外的内容](offset.zh.md)
* [Rotate: 旋转图片以不同角度查看内容](rotate.zh.md)
* [Locate: 定位到图片的任意](locate.zh.md)
* [Keep Transform: 切换图像时保持变换状态](keep_transform.zh.md)
* [Read Mode: 长图初始时充满屏幕方便阅读](readmode.zh.md)
* [Click: 接收点击事件](click.zh.md)
* [Subsampling: 通过子采样的方式显示大图避免 OOM](subsampling.zh.md)
* [Scroll Bar: 显示水平和垂直滚动条](scrollbar.zh.md)
* [Log: 修改日志等级以及输出管道](log.zh.md)
* [Modifier.zoom()](modifier_zoom.zh.md)

[ZoomImage]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: ../zoomimage-compose-coil3/src/commonMain/kotlin/com/github/panpf/zoomimage/SingletonCoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: ../zoomimage-compose-glide/src/main/kotlin/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: ../zoomimage-compose-sketch4/src/commonMain/kotlin/com/github/panpf/zoomimage/SingletonSketchZoomAsyncImage.kt

[ZoomImageSample]: ../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/BasicZoomImageSample.kt

[CoilZoomAsyncImageSample]: ../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/CoilZoomAsyncImageSample.common.kt

[GlideZoomAsyncImageSample]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: ../sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/SketchZoomAsyncImageSample.kt

[ZoomImageView]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: ../zoomimage-view-coil3-core/src/main/kotlin/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: ../zoomimage-view-glide/src/main/kotlin/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: ../zoomimage-view-picasso/src/main/kotlin/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: ../zoomimage-view-sketch4-core/src/main/kotlin/com/github/panpf/zoomimage/SketchZoomImageView.kt

[ZoomImageViewFragment]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/BasicZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: ../sample/src/androidMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples/SketchZoomImageViewFragment.kt

[Sketch]: https://github.com/panpf/sketch

[SketchAsyncImage]: https://github.com/panpf/sketch/blob/main/sketch-compose/src/commonMain/kotlin/com/github/panpf/sketch/compose/AsyncImage.kt

[Coil]: https://github.com/coil-kt/coil

[CoilAsyncImage]: https://github.com/coil-kt/coil/blob/main/coil-compose-singleton/src/commonMain/kotlin/coil/compose/SingletonAsyncImage.kt

[Glide]: https://github.com/bumptech/glide

[GlideImage]: https://github.com/bumptech/glide/blob/master/integration/compose/src/commonMain/kotlin/com/bumptech/glide/integration/compose/GlideImage.kt

[Picasso]: https://github.com/square/picasso

[ZoomableState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/zoom/ZoomableState.kt

[SubsamplingState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/subsampling/SubsamplingState.kt

[ZoomableEngine]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/view/zoom/ZoomableEngine.kt

[SubsamplingEngine]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/view/subsampling/SubsamplingEngine.kt

[ZoomImageView]: ../zoomimage-view/src/main/kotlin/com/github/panpf/zoomimage/ZoomImageView.kt

[ZoomImage]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/ZoomImage.kt

[ZoomState]: ../zoomimage-compose/src/commonMain/kotlin/com/github/panpf/zoomimage/compose/ZoomState.kt