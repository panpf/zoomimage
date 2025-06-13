# ![logo_image] ZoomImage

![Platform][platform_image]
![Platform2][platform_image2]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

Translations: [简体中文](README.zh.md)

ZoomImage is an gesture zoom viewing of images library specially designed for Compose Multiplatform
and Android View. It has the following features and functions:

* `Compose Multiplatform`. Support for Compose Multiplatform, which can be used on Android, macOS,
  Windows, Linux and other platforms
* `Power`. Supports basic functions such as double-click scale, two-finger scale, single-finger
  scale, mouse wheel scale, keyboard scale, single-finger drag, inertial sliding, and keyboard drag.
* `Locate`. Support for locate anywhere in the image and keeping it in the center of the screen
* `Rotate`. Supports 0°, 90°, 180°, 270°, 360° rotation of pictures
* `Subsampling`. Support for subsampling of very large images to avoid OOM, tile support animation,
  and sharpness gradients
* `Dynamic scale factor`. Automatically calculates the most appropriate double-click scaling factor
  based on image size and container size
* `Scaling damping`. When manually scaled beyond the maximum or minimum scale factor, there is a
  damped rubber band effect
* `Scroll bar`. Supports displaying horizontal and vertical scroll bars to clarify the current
  scroll position
* `Read Mode`. When a long image is displayed in reading mode, the initial state automatically fills
  the screen, and the user can immediately start reading the image content, eliminating the need for
  the user to double-click to scale in
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

## Sample App

* For Android, iOS, desktop version, and web deployable packages, please go to
  the [Releases](https://github.com/panpf/zoomimage/releases) page to download.
* Web example：https://panpf.github.io/zoomimage/app

## Download

`Published to mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (Not included 'v')

Compose multiplatform:

```kotlin
// Provides the SketchZoomAsyncImage component adapted to the Sketch v4+ image loader (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch4:${LAST_VERSION}")

// Provides SketchZoomAsyncImage component adapted to the old Sketch v3 image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch3:${LAST_VERSION}")

// Provides the CoilZoomAsyncImage component adapted to the Coil v3+ image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil3:${LAST_VERSION}")

// Provides CoilZoomAsyncImage component adapted to the old Coil v2 image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil2:${LAST_VERSION}")

// Provides basic ZoomImage component, additional work needs to be done to support subsampling, and does not support network images.
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")

// Support loading images from composeResources folder
implementation("io.github.panpf.zoomimage:zoomimage-compose-resources:${LAST_VERSION}")
```

> [!TIP]
> Just choose one according to the image loader you use or your needs.

Only android compose:

```kotlin
// Provides the GlideZoomAsyncImage component adapted to the Glide image loader
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")
```

> [!TIP]
> Why is there no picasso version of the compose ZoomImage component? Because Picasso has officially
> stated that it will not provide support for compose ([Original post here][picasso_compose_post])

Android view:

```kotlin
// Provides the SketchZoomImageView component adapted to the Sketch v4+ image loader (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch4:${LAST_VERSION}")

// Provides SketchZoomImageView component adapted to the old Sketch v3 image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch3:${LAST_VERSION}")

// Provides the CoilZoomImageView component adapted to the Coil v3+ image loader
implementation("io.github.panpf.zoomimage:zoomimage-view-coil3:${LAST_VERSION}")

// Provides CoilZoomImageView component adapted to the old Coil v2 image loader
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

Compose multiplatform:

```kotlin
// Use basic ZoomImage components
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

// Use SketchZoomAsyncImage component
SketchZoomAsyncImage(
    uri = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

// Use CoilZoomAsyncImage component
CoilZoomAsyncImage(
    model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> The usage of SketchZoomAsyncImage and CoilZoomAsyncImage is the same as their original AsyncImage,
> except that there is an additional `zoomState: ZoomState` parameter

Only android compose:

```kotlin
// Use GlideZoomAsyncImage component
GlideZoomAsyncImage(
    model = "https://sample.com/sample.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> The usage of GlideZoomAsyncImage is the same as its original GlideImage, except that there is an
> additional `zoomState: ZoomState` parameter

Android view:

```kotlin
// Use basis ZoomImageView component
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_world_thumbnail)
zoomImageView.setSubsamplingImage(ImageSource.fromResource(R.raw.huge_world))

// Use SketchZoomAsyncImage component
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("https://sample.com/sample.jpeg")

// Use CoilZoomImageView component
val coilZoomImageView = CoilZoomImageView(context)
sketchZoomImageView.loadImage("https://sample.com/sample.jpeg")

// Use GlideZoomImageView component
val glideZoomImageView = GlideZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
    .load("https://sample.com/sample.jpeg")
    .into(glideZoomImageView)

// Use PicassoZoomImageView component
val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageView.loadImage("https://sample.com/sample.jpeg")
```

## Document

* [Get Started](docs/getting_started.md)
* [Scale: Scale the image to see clearer details](docs/scale.md)
* [Offset: Move the image to see content outside the container](docs/offset.md)
* [Rotate: Rotate the image to view content from different angles](docs/rotate.md)
* [Locate: Locate anywhere in the image](docs/locate.md)
* [Keep Transform: Keep transform when switching images](docs/keep_transform.md)
* [Read Mode: Long images initially fill the screen for easy reading](docs/readmode.md)
* [Click: Receive click events](docs/click.md)
* [Subsampling: Display large images through subsampling to avoid OOM](docs/subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars](docs/scrollbar.md)
* [Log: Modify log level and output pipeline](docs/log.md)
* [Modifier.zoom()](docs/modifier_zoom.md)

## Samples

You can find the sample code in
the [examples](sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples)
directory, or you can go to [release](https://github.com/panpf/zoomimage/releases) page download
App experience

## Change Log

Please review the [CHANGELOG](CHANGELOG.md) file

## Test Platform

* Android: Emulator; Arm64; API 21-34
* Desktop: macOS; 14.6.1; JDK 17
* iOS: iphone 16 simulator; iOS 18.1
* Web: Chrome; 130

## Run Sample App

Prepare the environment:

1. Android Studio: Norwhal+ (2025.1.1+)
2. JDK: 17+
3. Use [kdoctor] to check the running environment and follow the prompts to install the required
   software
4. Android Studio installs the `Kotlin Multiplatform` plugin

Run the sample app:

1. Clone the project and open it using Android Studio
2. After synchronization is completed, the `Kotlin Multiplatform` plug-in will automatically create
   a running configuration for each platform.
3. Select the corresponding platform's running configuration, and then click Run

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

[logo_image]: docs/images/logo_mini.png

[platform_image]: https://img.shields.io/badge/Platform-Android-brightgreen.svg

[platform_image2]: https://img.shields.io/badge/Platform-ComposeMultiplatform-brightblue.svg

[license_image]: https://img.shields.io/badge/License-Apache%202-blue.svg

[license_link]: https://www.apache.org/licenses/LICENSE-2.0

[version_icon]: https://img.shields.io/maven-central/v/io.github.panpf.zoomimage/zoomimage-compose

[version_link]: https://repo1.maven.org/maven2/io/github/panpf/zoomimage/

[min_api_image]: https://img.shields.io/badge/AndroidAPI-21%2B-orange.svg

[min_api_link]: https://android-arsenal.com/api?level=21

[Sketch]: https://github.com/panpf/sketch

[Coil]: https://github.com/coil-kt/coil

[Glide]: https://github.com/bumptech/glide

[Picasso]: https://github.com/square/picasso

[picasso_compose_post]: https://github.com/square/picasso/issues/2203#issuecomment-826444442

[kdoctor]: https://github.com/Kotlin/kdoctor