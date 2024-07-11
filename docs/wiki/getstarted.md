## Get Started

Translations: [简体中文](getstarted_zh.md)

> * The following example takes precedence over the Compose version component for demonstration
> * [ZoomState].zoomable is equivalent to [ZoomImageView].zoomable
> * [ZoomState].subsampling is equivalent to [ZoomImageView].subsampling

### Components

The ZoomImage library includes several components to choose from, so you can choose the right one
for your needs.

*Different components need to import different dependencies, please refer
to [README](../../README.md) to import the
corresponding dependencies*

compose android：

* [SketchZoomAsyncImage]：`recommended`
  * Zoom Image component integrated with [Sketch] image loading library, the usage is the same as
    the [AsyncImage] [SketchAsyncImage] component of [Sketch].
  * Network images and subsampling are already supported without any additional work
  * Reference Example [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：
  * Zoom Image component integrated with the [Coil] image loading library, the usage is the same
    as the [AsyncImage][CoilAsyncImage] component of [Coil].
  * Network images and subsampling are already supported without any additional work
  * Reference example [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：
    * Zoom Image component that integrates the [Glide] image loading library, the usage is the same
      as the [GlideImage] component of [Glide].
    * Network images and subsampling are already supported without any additional work
    * Reference example [GlideZoomAsyncImageSample]

compose multiplatform：

* [ZoomImage]：
    * The most basic zoom Image component, not integrate the image loading library
    * Additional work needs to be done to support network pictures and subsampling
    * Reference example [ZoomImageSample]

view：

* [SketchZoomImageView]：`recommended`
  * Zoom ImageView with integrated [Sketch] image loading library
  * Adapted [Sketch] supports subsampling without any additional work
  * Reference example [SketchZoomImageViewFragment]
* [CoilZoomImageView]：
  * Zoomed ImageView with integrated [Coil] image loading library
  * Adapted [Coil] supports subsampling without any additional work
  * Reference example [CoilZoomImageViewFragment]
* [GlideZoomImageView]：
    * Zoomed ImageView with integrated [Glide] image loading library
    * Adapted [Glide] supports subsampling without any additional work
    * Reference example [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：
    * Zoomed ImageView integrated with the [Picasso] image loading library
    * Adapted [Picasso] supports subsampling without any additional work
    * Reference example [PicassoZoomImageViewFragment]
* [ZoomImageView]：
    * The most basic zoom ImageView, not integrating the image loading library
    * Additional work needs to be done to support network pictures and subsampling
    * Reference example [ZoomImageViewFragment]

Summary：

* Components with integrated image loaders can support image and subsampling from any source without
  any additional work
* Components that do not integrate an image loader can only display local images and require an
  additional call to the `subsampling.setImageSource(ImageSource)` method to support subsampling
  functionality

### Examples

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

/* 
 * desktop
 */
val zoomState: ZoomState by rememberZoomState()
LaunchedEffect(Unit) {
  zoomState.subsampling.setImageSource(ImageSource.fromResource("huge_image.jpeg"))
}
ZoomImage(
    painter = painterResource("huge_image_thumbnail.jpeg"),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
  zoomState = zoomState,
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

> PicassoZoomImageView provides a set of specialized APIs to listen for load results and get URIs,
> to support subsampling, so please don't load images directly using the official API

The APIs for zoom and subsampling are encapsulated in separate classes, and the compose versions
are [ZoomableState] and [SubsamplingState], view The versions are [ZoomableEngine]
and [SubsamplingEngine]

example：

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
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

*For more detailed information about scale, offset, rotation, subsampling, read mode, scroll bar
and other functions, please refer to the documentation at the end of the page*

### Public Properties

```kotlin
// compose
val zoomState: ZoomState by rememberZoomState()
SketchZoomAsyncImage(zoomState = zoomState)
val zoomable: ZoomableState = zoomState.zoomable
val subsampling: SubsamplingState = zoomState.subsampling

// view
val sketchZoomImageView = SketchZoomImageView(context)
val zoomable: ZoomableEngine = sketchZoomImageView.zoomable
val subsampling: SubsamplingEngine = sketchZoomImageView.subsampling
```

> * Note: The relevant properties of the view version are wrapped in StateFlow, so its name is
    suffixed with State compared to the compose version

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

#### Listen property changed

* The relevant properties of the compose version are wrapped in State and can be read directly in
  the Composable function to implement listening
* The relevant properties of the view are wrapped in StateFlow, and its collect function can be
  called to implement the listening

## Document

* [Scale: scale, double-click scale, duration setting](scale.md)
* [Offset: Move to the specified position](offset.md)
* [Locate: Locate anywhere in the image and keeping it in the center of the screen](locate.md)
* [Rotate: Rotate the image](rotate.md)
* [Read Mode: Long images initially fill the screen for easy reading](readmode.md)
* [Click: Receive click events](click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM](subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars to clarify the current scroll position](scrollbar.md)
* [Log: Modify log level and output pipeline](log.md)
* [Compose Multiplatform: Use on desktop platform](multiplatform.md)

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