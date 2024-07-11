# ![logo_image] ZoomImage

![Platform][platform_image]
![Platform2][platform_image2]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

Translations: [简体中文](README_zh.md)

ZoomImage is a library designed for Compose Multiplatform and Android View for gesture zoom viewing
of images. It has the following features and functions:

* `Compose Multiplatform`. Support for Compose Multiplatform, which can be used on Android, macOS,
  Windows, Linux and other platforms
* `Power`. Support basic functions such as double-click zoom, gesture zoom, single-finger drag,
  and inertial swipe
* `Locate`. Support for locate anywhere in the image and keeping it in the center of the screen
* `Rotate`. Supports 0°, 90°, 180°, 270°, 360° rotation of pictures
* `Subsampling`. Support for subsampling of very large images to avoid OOM, tile support animation,
  and sharpness gradients
* `Dynamic scale factor`. Automatically calculates the most appropriate double-click scaling factor
  based
  on image size and container size
* `Scaling damping`. When manually scaled beyond the maximum or minimum zoom factor, there is a
  damped rubber band effect
* `One-finger scale`. Double-tap and hold the screen and slide up or down to zoom the image
* `Scroll bar`. Supports displaying horizontal and vertical scroll bars to clarify the current
  scroll position
* `Read Mode`. When a long image is displayed in reading mode, the initial state automatically fills
  the screen, and the user can immediately start reading the image content, eliminating the need for
  the user to double-click to zoom in
* `Exif`. Support reading Exif Orientation information and automatically rotating images
* `Image Loader`. Provide support for image loaders such as sketch, coil, glide, picasso, etc., and
  can also customize support for more image loaders

https://github.com/panpf/zoomimage/assets/3250512/f067bed9-24e4-4ab8-a839-0731e155f4ef

## Multiplatform support

| Function/Platform    | Android | iOS | Desktop | Web |
|:---------------------|:-------:|:---:|:-------:|:---:|
| Zoom                 |    ✅    |  ✅  |    ✅    |  ✅  |
| Subsampling          |    ✅    |  ✅  |    ✅    |  ✅  |
| Exif Orientation     |    ✅    |  ✅  |    ✅    |  ✅  |
| Integrated [Sketch]  |    ✅    |  ✅  |    ✅    |  ✅  |
| Integrated [Coil]    |    ✅    |  ✅  |    ✅    |  ✅  |
| Integrated [Glide]   |    ✅    |  ❌  |    ❌    |  ❌  |
| Integrated [Picasso] |    ✅    |  ❌  |    ❌    |  ❌  |

## Download

`Published to mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (Not included 'v')

Compose Multiplatform:

```kotlin
// Provides the SketchZoomAsyncImage component adapted to the Sketch image loader (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch:${LAST_VERSION}")

// Provides SketchZoomAsyncImage component adapted to the old Sketch 3 image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch3:${LAST_VERSION}")

// Provides the CoilZoomAsyncImage component adapted to the Coil image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil:${LAST_VERSION}")

// Provides CoilZoomAsyncImage component adapted to the old Coil 2 image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil2:${LAST_VERSION}")

// Provides basic ZoomImage component, additional work needs to be done to support subsampling, and does not support network images.
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")

// Support loading images from composeResources folder
implementation("io.github.panpf.zoomimage:zoomimage-compose-resources:${LAST_VERSION}")
```

> [!TIP]
> Just choose one according to the image loader you use or your needs.

Only Android Compose:

```kotlin
// Provides the GlideZoomAsyncImage component adapted to the Glide image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")
```

> [!TIP]
> Why is there no picasso version of the compose ZoomImage component? Because Picasso has officially
> stated that it will not provide support for compose ([Original post here][picasso_compose_post])

Android View:

```kotlin
// Provides the SketchZoomImageView component adapted to the Sketch image loader (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch:${LAST_VERSION}")

// Provides SketchZoomImageView component adapted to the old Sketch 3 image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch3:${LAST_VERSION}")

// Provides the CoilZoomImageView component adapted to the Coil image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-coil:${LAST_VERSION}")

// Provides CoilZoomImageView component adapted to the old Coil 2 image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-coil2:${LAST_VERSION}")

// Provides the GlideZoomImageView component adapted to the Glide image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-glide:${LAST_VERSION}")

// Provides the PicassoZoomImageView component adapted to the Picasso image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-picasso:${LAST_VERSION}")

// Provides the most basic ZoomImageView component. Additional work needs to be done to support subsampling. Network images are not supported.
implementation("io.github.panpf.zoomimage:zoomimage-view:${LAST_VERSION}")
```

> [!TIP]
> Just choose one according to the image loader you use or your needs.

### R8 / Proguard

ZoomImage's own obfuscation is already included in aar, but you may also need to add obfuscation
configuration for other libraries that depend indirectly

## Quickly Started

Compose Multiplatform：

```kotlin
// Use basic ZoomImage components
val zoomState: ZoomState by rememberZoomState()
LaunchedEffect(Unit) {
    val resUri = Res.getUri("files/huge_china.jpeg")
    zoomState.subsampling.setImageSource(ImageSource.fromResource(resUri))
}
ZoomImage(
    painter = painterResource(Res.drawable.huge_china_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)

// Use SketchZoomAsyncImage component
SketchZoomAsyncImage(
    uri = "http://sample.com/huge_china.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

// Use CoilZoomAsyncImage component
CoilZoomAsyncImage(
    model = "http://sample.com/huge_china.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> The usage of SketchZoomAsyncImage and CoilZoomAsyncImage is the same as their original AsyncImage,
> except that there is an additional `zoomState: ZoomState` parameter

Only Android Compose：

```kotlin
// Use GlideZoomAsyncImage component
GlideZoomAsyncImage(
    model = "http://sample.com/huge_china.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> The usage of GlideZoomAsyncImage is the same as its original GlideImage, except that there is an
> additional `zoomState: ZoomState` parameter

Android View：

```kotlin
// Use basis ZoomImageView component
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_china_thumbnail)
zoomImageView.subsampling.setImageSource(ImageSource.resource(R.raw.huge_china))

// Use GlideZoomAsyncImage component
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/huge_china.jpeg")

// Use CoilZoomImageView component
val coilZoomImageView = CoilZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/huge_china.jpeg")

// Use CoilZoomImageView component
val glideZoomImageView = CoilZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
    .load("http://sample.com/huge_china.jpeg")
    .into(glideZoomImageView)

// Use PicassoZoomImageView component
val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageView.loadImage("http://sample.com/huge_china.jpeg")
```

## Document

* [Get Started](docs/wiki/getstarted.md)
* [Scale: scale, double-click scale, duration setting](docs/wiki/scale.md)
* [Offset: Move to the specified position](docs/wiki/offset.md)
* [Locate: Locate anywhere in the image](docs/wiki/locate.md)
* [Rotate: Rotate the image](docs/wiki/rotate.md)
* [Read Mode: Long images initially fill the screen for easy reading](docs/wiki/readmode.md)
* [Click: Receive click events](docs/wiki/click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM](docs/wiki/subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars](docs/wiki/scrollbar.md)
* [Log: Modify log level and output pipeline](docs/wiki/log.md)

## Samples

You can find the sample code in
the [examples](sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples)
directory, or you can go to [release](https://github.com/panpf/zoomimage/releases) page download
App experience

## Change Log

Please review the [CHANGELOG](CHANGELOG.md) file

## Run Sample App

Prepare the environment:

1. Android Studio: Koala+ (2024.1.1+)
2. JDK: 17+
3. Use [kdoctor] to check the running environment and follow the prompts to install the required
   software
4. Android Studio installs the `Kotlin Multiplatform` and `Compose Multiplatform IDE Support`plugins

Run the sample app:

1. Clone the project and open it using Android Studio
2. The running configurations of each platform have been added to the `.run` directory. After
   synchronization is completed, directly select the running configuration of the corresponding
   platform in the running configuration drop-down box at the top of Android Studio and click Run.
3. The running configuration of the ios platform requires you to manually create it according to the
   template, as follows:
    1. Copy the `.run/iosSample.run.template.xml` file and remove the `.template` suffix.
       The `.ignore` file has been configured to ignore `iosSample.run.xml`
    2. Click `Edit Configurations` in the run configuration drop-down box at the top,
       select `iosSample` and then configure `Execute target`

## Comparison of similar libraries

| Function/Library      | ZoomImage | [Telephoto] | [PhotoView] | [Subsampling<br/>ScaleImageView] |
|:----------------------|:---------:|:-----------:|:-----------:|:--------------------------------:|
| Compose Multiplatform |     ✅     |      ✅      |      ❌      |                ❌                 |
| Android Compose       |     ✅     |      ✅      |      ❌      |                ❌                 |
| Android View          |     ✅     |      ❌      |      ✅      |                ✅                 |
| Rotate                |     ✅     |      ❌      |      ✅      |                ❌                 |
| Locate                |     ✅     |      ❌      |      ❌      |                ✅                 |
| Scroll Bar            |     ✅     |      ❌      |      ❌      |                ❌                 |
| Read Mode             |     ✅     |      ❌      |      ❌      |                ❌                 |
| Subsampling           |     ✅     |      ✅      |      ❌      |                ✅                 |
| Subsampling animation |     ✅     |      ❌      |      ❌      |                ❌                 |
| One-finger scale      |     ✅     |      ✅      |      ✅      |                ✅                 |
| Dynamic scale factor  |     ✅     |      ❌      |      ❌      |                ❌                 |
| Image Loader          |     ✅     |      ✅      |      ❌      |                ❌                 |
| Rich interfaces       |     ✅     |      ❌      |      ✅      |                ✅                 |

## My Projects

The following are my other open source projects. If you are interested, you can learn about them:

* [sketch](https://github.com/panpf/sketch): Sketch is an image loading library designed for Compose
  Multiplatform and Android View. It is powerful and rich in functions. In addition to basic
  functions, it also supports GIF, SVG, video thumbnails, Exif Orientation, etc.
* [assembly-adapter](https://github.com/panpf/assembly-adapter): A library on Android that provides
  multi-type Item implementations for various adapters. Incidentally, it also provides the most
  powerful divider for RecyclerView.
* [sticky-item-decoration](https://github.com/panpf/stickyitemdecoration): RecyclerView sticky item
  implementation

## License

Apache 2.0. See the [LICENSE](LICENSE.txt) file for details.

[logo_image]: docs/res/logo_mini.png

[platform_image]: https://img.shields.io/badge/Platform-Android-brightgreen.svg

[platform_image2]: https://img.shields.io/badge/Platform-ComposeMultiplatform-brightblue.svg

[license_image]: https://img.shields.io/badge/License-Apache%202-blue.svg

[license_link]: https://www.apache.org/licenses/LICENSE-2.0

[version_icon]: https://img.shields.io/maven-central/v/io.github.panpf.zoomimage/zoomimage-compose

[version_link]: https://repo1.maven.org/maven2/io/github/panpf/zoomimage/

[min_api_image]: https://img.shields.io/badge/AndroidAPI-16%2B-orange.svg

[min_api_link]: https://android-arsenal.com/api?level=16

[Telephoto]: https://github.com/saket/telephoto

[PhotoView]: https://github.com/Baseflow/PhotoView

[Subsampling<br/>ScaleImageView]: https://github.com/davemorrissey/subsampling-scale-image-view

[Sketch]: https://github.com/panpf/sketch

[Coil]: https://github.com/coil-kt/coil

[Glide]: https://github.com/bumptech/glide

[Picasso]: https://github.com/square/picasso

[picasso_compose_post]: https://github.com/square/picasso/issues/2203#issuecomment-826444442

[kdoctor]: https://github.com/Kotlin/kdoctor