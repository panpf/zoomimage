## Get Started

Translations: [简体中文](getting_started.zh.md)

## Components

The ZoomImage library includes several components to choose from, so you can choose the right one
for your needs.

Compose multiplatform：

* [SketchZoomAsyncImage]：Integrated [Sketch] image loading library, support network images and
  subsampling. Example: [SketchZoomAsyncImageSample]. `Recommended`
* [CoilZoomAsyncImage]：Integrated [Coil]
  image loading library, support network images and subsampling. Example: [CoilZoomAsyncImageSample]
* [ZoomImage]：The basic zoom component does not integrate the image loading library and does not
  support network images. You need to call `subsampling.setImageResource()`
  method to support subsampling. Example: [ZoomImageSample]

Only android compose：

* [GlideZoomAsyncImage]：Integrated [Glide]
  image loading library, support network images and subsampling.
  Example: [GlideZoomAsyncImageSample]

Android view：

* [SketchZoomImageView]：Integrated [Sketch]
  image loading library, support network images and subsampling.
  Example: [SketchZoomImageViewFragment]. `Recommended`
* [CoilZoomImageView]：Integrated [Coil]
  image loading library, support network images and subsampling.
  Example: [CoilZoomImageViewFragment]
* [GlideZoomImageView]：Integrated [Glide]
  image loading library, support network images and subsampling.
  Example: [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：Integrated [Picasso]
  image loading library, support network images and subsampling.
  Example: [PicassoZoomImageViewFragment]
* [ZoomImageView]
  ：The basic zoom component does not integrate the image loading library and does not support
  network images. You need to call `subsampling.setImageResource()`
  method to support subsampling. Example: [ZoomImageViewFragment]

> [!TIP]
> * Different components need to import different dependencies, please refer
    to [README](../README.md#download) to import the corresponding dependencies*
> * Components with integrated image loaders can support image and subsampling from any source
    without any additional work
> * Components that do not integrate an image loader can only display local images and require an
    additional call to the `setSubsamplingImage()` method to support subsampling
    functionality

## Examples

Compose multiplatform：

```kotlin
// Using the basic ZoomImage component
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

// Using the SketchZoomAsyncImage component
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

// Using the CoilZoomAsyncImage component
CoilZoomAsyncImage(
    model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

Only android compose：

```kotlin
// Using the GlideZoomAsyncImage component
GlideZoomAsyncImage(
    model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

Android view：

```kotlin
// Using the basic ZoomImageImage component
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_world_thumbnail)
val imageSource = ImageSource.fromResource(context, R.raw.huge_world)
zoomImageView.setSubsamplingImage(imageSource)

// Using the SketchZoomImageView component
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("https://sample.com/sample.jpeg")

// Using the CoilZoomImageView component
val coilZoomImageView = CoilZoomImageView(context)
coilZoomImageView.load("https://sample.com/sample.jpeg")

// Using the GlideZoomImageView component
val glideZoomImageView = GlideZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
    .load("https://sample.com/sample.jpeg")
    .into(glideZoomImageView)

// Using the PicassoZoomImageView component
val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageViewImage.loadImage("https://sample.com/sample.jpeg")
```

> [!TIP]
> * PicassoZoomImageView provides a set of specialized APIs to listen for load results and get URIs,
    to support subsampling, so please don't load images directly using the official API
> * For more usage methods related to image loading of each component, please refer to the usage of
    its original component.

## Zoom And Subsampling

The scaling and subsampling APIs are encapsulated in different classes. You can directly control
scaling and subsampling or obtain related information through them, as follows:

* compose versions are [ZoomableState] and [SubsamplingState]
* view versions are [ZoomableEngine] and [SubsamplingEngine]

Example:

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

### Public Properties

> [!TIP] Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
> suffixed with State compared to the compose version

Readable properties:

* `zoomable.containerSize: IntSize`: The size of the container that holds the content
* `zoomable.contentSize: IntSize`: The size of the content, usually Painter.intrinsicSize.round()
* `zoomable.contentOriginSize: IntSize`: The original size of the content
* `zoomable.contentScale: ContentScale`: The default scaling method of content is ContentScale.Fit
* `zoomable.alignment: Alignment`: The alignment of content in container is Alignment.TopStart by
  default
* `zoomable.layoutDirection: LayoutDirection`: The layout direction of container, the default is
  LayoutDirection.Ltr
* `zoomable.readMode: ReadMode?`: Reading mode configuration, default is null
* `zoomable.scalesCalculator: ScalesCalculator`: The minScale, mediumScale, and maxScale
  calculators, the default is ScalesCalculator.Dynamic
* `zoomable.threeStepScale: Boolean`: Whether to zoom in between minScale, mediumScale and maxScale
  when double-clicking to zoom, default is false
* `zoomable.rubberBandScale: Boolean`: Whether to enable the rubber band effect, the default is true
* `zoomable.oneFingerScaleSpec: OneFingerScaleSpec`: Single-referential scaling configuration,
  default is OneFingerScaleSpec.Default
* `zoomable.animationSpec: ZoomAnimationSpec`: Animation configurations such as zoom, offset, etc.,
  default is ZoomAnimationSpec.Default
* `zoomable.limitOffsetWithinBaseVisibleRect: Boolean`: Whether to limit the offset to
  contentBaseVisibleRect, the default is false
* `zoomable.containerWhitespaceMultiple: Float`: Add blank space around the container based on
  multiples of the container size, the default is 0f
* `zoomable.containerWhitespace: ContainerWhitespace`: The configuration of blank areas around the
  container has higher priority than containerWhitespaceMultiple, and the default is
  ContainerWhitespace.Zero
* `zoomable.keepTransformWhenSameAspectRatioContentSizeChanged: Boolean`: Whether to keep transform
  unchanged when the contentSize of the same aspect ratio is changed, the default is false
* `zoomable.disabledGestureTypes: Int`: Configure the disabled gesture type, the default is 0 (no
  gesture is disabled), and multiple gesture types can be combined using the bits or actions of
  GestureType
* `zoomable.reverseMouseWheelScale: Boolean`: Whether to reverse the direction of the mouse wheel,
  the default is false
* `zoomable.mouseWheelScaleCalculator: MouseWheelScaleCalculator`: Mouse wheel zoom calculator, the
  default is MouseWheelScaleCalculator.Default
* `zoomable.transform: Transform`:Current transform status (baseTransform + userTransform)
* `zoomable.baseTransform: Transform`: The current basic transformation state is affected by
  contentScale and alignment parameters
* `zoomable.userTransform: Transform`: The current user's transformation status is affected by
  scale(), location(), user gesture scaling, double-clicking and other operations
* `zoomable.minScale: Float`: Minimum scale factor, for limits the final scale factor, and as a
  target value for one of when switch scale
* `zoomable.mediumScale: Float`: Medium scale factor, only as a target value for one of when switch
  scale
* `zoomable.maxScale: Float`: Maximum scale factor, for limits the final scale factor, and as a
  target value for one of when switch scale
* `zoomable.continuousTransformType: Int`: If true, a transformation is currently in progress,
  possibly in a continuous gesture operation, or an animation is in progress
* `zoomable.contentBaseDisplayRectF: Rect`: The content region in the container after the
  baseTransform transformation
* `zoomable.contentBaseDisplayRect: IntRect`: The content region in the container after the
  baseTransform transformation
* `zoomable.contentBaseVisibleRectF: Rect`: The content is visible region to the user after the
  baseTransform transformation
* `zoomable.contentBaseVisibleRect: IntRect`: The content is visible region to the user after the
  baseTransform transformation
* `zoomable.contentDisplayRectF: Rect`: The content region in the container after the final
  transform transformation
* `zoomable.contentDisplayRect: IntRect`: The content region in the container after the final
  transform transformation
* `zoomable.contentVisibleRectF: Rect`: The content is visible region to the user after the final
  transform transformation
* `zoomable.contentVisibleRect: IntRect`: The content is visible region to the user after the final
  transform transformation
* `zoomable.sourceScaleFactor: ScaleFactor`: Scaling ratio based on the original image
* `zoomable.sourceVisibleRectF: Rect`: contentVisibleRect maps to the area on the original image
* `zoomable.sourceVisibleRect: IntRect`: contentVisibleRect maps to the area on the original image
* `zoomable.scrollEdge: ScrollEdge`: Edge state for the current offset

* `subsampling.disabled: Boolean`: Whether to disable subsampling function
* `subsampling.tileImageCache: TileImageCache?`: The memory cache of Tile tile is null by default.
* `subsampling.disabledTileImageCache: Boolean`: Whether to disable the memory cache of Tile tile,
  default to false
* `subsampling.tileAnimationSpec: TileAnimationSpec`: The configuration of tile animation is default
  to TileAnimationSpec.Default
* `subsampling.pausedContinuousTransformTypes: Int`: Pauses the configuration of continuous
  transformation types for loading tiles
* `subsampling.disabledBackgroundTiles: Boolean`: Whether to disable background tile, default to
  false
* `subsampling.stopped: Boolean`: Whether to stop loading tiles, default to false
* `subsampling.disabledAutoStopWithLifecycle: Boolean`: Whether to disable automatic stop loading of
  tiles based on Lifecycle, default to false
* `subsampling.regionDecoders: List<RegionDecoder.Factory>`: Add a custom RegionDecoder, default to
  an empty list
* `subsampling.showTileBounds: Boolean`: Whether to display the boundary of Tile, default to false
* `subsampling.ready: Boolean`: Whether the image is ready for subsampling
* `subsampling.imageInfo: ImageInfo`: The information of the image, including width, height, format,
  exif information, etc
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`: Tile grid size map
* `subsampling.sampleSize: Int`: The sample size of the image
* `subsampling.imageLoadRect: IntRect`: The image load rect
* `subsampling.foregroundTiles: List<TileSnapshot>`: List of current foreground tiles
* `subsampling.backgroundTiles: List<TileSnapshot>`: List of current background tiles

Interactive methods:

* `zoomable.setReadMode(ReadMode?)`: Setting up reading mode configuration
* `zoomable.setScalesCalculator(ScalesCalculator)`: Set minScale, mediumScale, and maxScale
  calculator
* `zoomable.setThreeStepScale(Boolean)`: Set whether to cyclically scale between minScale,
  mediumScale, and maxScale when double-clicking to zoom
* `zoomable.setRubberBandScale(Boolean)`: Set whether to use rubber band effect after scaling
  exceeds minScale or maxScale
* `zoomable.setOneFingerScaleSpec(OneFingerScaleSpec)`: Set single finger zoom configuration
* `zoomable.setAnimationSpec(ZoomAnimationSpec)`: Set animation configurations such as zoom, offset,
  etc.
* `zoomable.setLimitOffsetWithinBaseVisibleRect(Boolean)`: Set whether to limit offsets to
  contentBaseVisibleRect
* `zoomable.setContainerWhitespaceMultiple(Float)`: Set multiples based on container size to add
  blank areas around the container
* `zoomable.setContainerWhitespace(ContainerWhitespace)`: Set the configuration of blank areas
  around the container, with priority higher than containerWhitespaceMultiple
* `zoomable.setKeepTransformWhenSameAspectRatioContentSizeChanged(Boolean)`: Set whether the
  transform remains unchanged when the contentSize of the same aspect ratio is changed
* `zoomable.setDisabledGestureTypes(Int)`: Set the disabled gesture type, you can use the bits or
  actions of GestureType to combine multiple gesture types
* `zoomable.setReverseMouseWheelScale(Boolean)`: Set whether to reverse the direction of the mouse
  wheel
* `zoomable.setMouseWheelScaleCalculator(MouseWheelScaleCalculator)`: Setting up the mouse wheel
  zoom calculator
* `zoomable.scale()`: Scaling content to the specified multiple
* `zoomable.scaleBy()`: Incrementally scale the multiple specified by content by multiplication
* `zoomable.scaleByPlus()`: Incrementally scale content specified multiples by addition
* `zoomable.switchScale()`: Switch the scaling multiple of the content, loop between minScale and
  mediumScale by default, if threeStepScale is true, loop between minScale, mediumScale and maxScale
* `zoomable.offset()`: Offset content to the specified location
* `zoomable.offsetBy()`: Offset as incremental content specified offset
* `zoomable.locate()`: Position to a specified position on the content, or scale to a specified
  multiple when used.
* `zoomable.rotate()`: Rotate content to the specified angle, the angle can only be multiples of 90
* `zoomable.rotateBy()`: Rotate the angle specified by content in incremental manner, the angle can
  only be multiples of 90.
* `zoomable.getNextStepScale(): Float`: Get the next scaling multiple, loop between minScale and
  mediumScale by default, if threeStepScale is true, loop between minScale, mediumScale and maxScale
* `zoomable.touchPointToContentPoint(): IntOffset`: Convert the touch point to a point on the
  content, the origin is the upper left corner of the content
* `zoomable.touchPointToContentPointF(): Offset`: Convert the touch point to a point on the content,
  the origin is the upper left corner of the content
* `zoomable.sourceToDraw(Offset): Offset`: Convert the points on the original image to the points at
  the time of drawing, the origin is the upper left corner of the container
* `zoomable.sourceToDraw(Rect): Rect`: Convert the rectangle on the original image to the rectangle
  when drawing, the origin is the upper left corner of the container
* `zoomable.canScroll(): Boolean`: Determine whether the current content can scroll in the specified
  direction

* `subsampling.setImage(): Boolean`: Set the subsampling image, return whether it is successful, the
  components that integrate the image loader will automatically set the subsampling image
* `subsampling.setDisabled(Boolean)`: Set whether to disable subsampling function
* `subsampling.setTileImageCache(TileImageCache?)`: Set the memory cache of Tile tile, and the
  components that integrate the picture loader will automatically set it
* `subsampling.setDisabledTileImageCache(Boolean)`: Set whether to disable the memory cache of Tile
  tiles
* `subsampling.setTileAnimationSpec(TileAnimationSpec)`: Set tile animation configuration
* `subsampling.setPausedContinuousTransformTypes(Int)`: Set the configuration of the continuous
  transformation type that pauses loading tile. Multiple types can be combined by bits or operators.
  The default is TileManager.DefaultPausedContinuousTransformType
* `subsampling.setDisabledBackgroundTiles(Boolean)`: Set whether to disable background tiles
* `subsampling.setStopped(Boolean)`: Set whether to stop loading tiles
* `subsampling.setDisabledAutoStopWithLifecycle(Boolean)`: Set whether to disable the automatic stop
  loading of tiles according to Lifecycle
* `subsampling.setRegionDecoders(List<RegionDecoder.Factory>)`: Set up a custom RegionDecoder
* `subsampling.setShowTileBounds(Boolean)`: Set whether to display the boundary of Tile

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

> [!TIP]
> For more detailed information about scale, offset, rotation, subsampling, read mode, scroll bar
> and other functions, please refer to the documentation at the end of the page*

## Document

* [Scale: Scale the image to see clearer details](scale.md)
* [Offset: Move the image to see content outside the container](offset.md)
* [Rotate: Rotate the image to view content from different angles](rotate.md)
* [Locate: Locate anywhere in the image](locate.md)
* [Keep Transform: Keep transform when switching images](keep_transform.md)
* [Read Mode: Long images initially fill the screen for easy reading](readmode.md)
* [Click: Receive click events](click.md)
* [Subsampling: Display large images through subsampling to avoid OOM](subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars](scrollbar.md)
* [Log: Modify log level and output pipeline](log.md)
* [Modifier.zoom()](modifier_zoom.md)

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