## Get Started

Translations: [简体中文](getstarted_zh.md)

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
    to [README](../../README.md#download) to import the corresponding dependencies*
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

example：

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

### Public Properties

> [!TIP] Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
> suffixed with State compared to the compose version

* `zoomable.baseTransform: Transform`: Base transformation, include the base scale, offset,
  rotation, which is affected by
  contentScale, alignment properties and rotate method
* `zoomable.userTransform: Transform`: User transformation, include the user scale, offset,
  rotation, which is affected by the user's
  gesture, readMode properties and scale, offset, locate method
* `zoomable.transform: Transform`: Final transformation, include the final scale, offset, rotation,
  is equivalent
  to `baseTransform + userTransform`
* `zoomable.minScale: Float`: Minimum scale factor, for limits the final scale factor, and as a
  target value for one of when
  switch scale
* `zoomable.mediumScale: Float`: Medium scale factor, only as a target value for one of when switch
  scale
* `zoomable.maxScale: Float`: Maximum scale factor, for limits the final scale factor, and as a
  target value for one of when
  switch scale
* `zoomable.continuousTransformType: Int`: If true, a transformation is currently in progress,
  possibly in a continuous gesture
  operation, or an animation is in progress
* `zoomable.contentBaseDisplayRect: IntRect`: The content region in the container after the
  baseTransform transformation
* `zoomable.contentBaseVisibleRect: IntRect`: The content is visible region to the user after the
  baseTransform transformation
* `zoomable.contentDisplayRect: IntRect`: The content region in the container after the final
  transform transformation
* `zoomable.contentVisibleRect: IntRect`: The content is visible region to the user after the final
  transform transformation
* `zoomable.scrollEdge: ScrollEdge`: Edge state for the current offset
* `zoomable.containerSize: IntSize`: The size of the container that holds the content
* `zoomable.contentSize: IntSize`: The size of the content, usually Painter.intrinsicSize.round()
* `zoomable.contentOriginSize: IntSize`: The original size of the content
* `subsampling.ready: Boolean`: Whether the image is ready for subsampling
* `subsampling.imageInfo: ImageInfo`: The information of the image, including width, height, format,
  exif information, etc
* `subsampling.exifOrientation: ExifOrientation`: The exif information of the image
* `subsampling.foregroundTiles: List<TileSnapshot>`: List of current foreground tiles
* `subsampling.backgroundTiles: List<TileSnapshot>`: List of current background tiles
* `subsampling.sampleSize: Int`: The sample size of the image
* `subsampling.imageLoadRect: IntRect`: The image load rect
* `subsampling.tileGridSizeMap: Map<Int, IntOffset>`: Tile grid size map

### Listen property changed

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
* [Read Mode: Long images initially fill the screen for easy reading](readmode.md)
* [Click: Receive click events](click.md)
* [Subsampling: Display large images through subsampling to avoid OOM](subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars](scrollbar.md)
* [Log: Modify log level and output pipeline](log.md)
* [Modifier.zoom()](modifier_zoom.md)

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