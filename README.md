# ![logo_image] ZoomImage

![Platform][platform_image]
![Platform2][platform_image2]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

Translations: [简体中文](README_zh.md)

Library for zoom images, supported Android View, Compose and Compose Multiplatform; supported
double-click zoom, One or two fingers gesture zoom, single-finger drag, inertial sliding,
positioning, rotation,
super-large image subsampling and other functions.

https://github.com/panpf/zoomimage/assets/3250512/f067bed9-24e4-4ab8-a839-0731e155f4ef

## Features

* `Complete`. Support basic functions such as double-click zoom, gesture zoom, single-finger drag,
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
* `Compose Multiplatform`. Support for Compose Multiplatform, which can be used on Android, macOS,
  Windows, Linux and other platforms

## Comparison of similar libraries

| Function/Library      | ZoomImage | [Telephoto] | [PhotoView] | [Subsampling<br/>ScaleImageView] |
|:----------------------|:---------:|:-----------:|:-----------:|:--------------------------------:|
| Compose               |     ✅     |      ✅      |      ❌      |                ❌                 |
| Compose Multiplatform |     ✅     |      ✅      |      ❌      |                ❌                 |
| View                  |     ✅     |      ❌      |      ✅      |                ✅                 |
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

## Import

`Published to mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (Not included 'v')

### compose android

`Choose according to the image loader you use`

```kotlin
// The SketchZoomAsyncImage component is provided with the Coil Image Loader, easy to use (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch:${LAST_VERSION}")

// The CoilZoomAsyncImage component is provided with the Coil Image Loader, easy to use
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil:${LAST_VERSION}")

// The GlideZoomAsyncImage component is provided with the Coil Image Loader, easy to use
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")
```

Why is there no picasso version of the compose ZoomImage component? Picasso has officially stated
that it will not provide compose
Support ([Reference](https://github.com/square/picasso/issues/2203#issuecomment-826444442))

### compose multiplatform

```kotlin
// Providing the basic ZoomImage component, additional work is required to support network images and subsampling 
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")
```

### view

`Choose according to the image loader you use`

```kotlin
// The SketchZoomImageView component is provided with the Sketch Image Loader, easy to use (recommended)
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Coil Image Loader, easy to use
implementation("io.github.panpf.zoomimage:zoomimage-view-coil:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Glide Image Loader, easy to use
implementation("io.github.panpf.zoomimage:zoomimage-view-glide:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Picasso Image Loader, easy to use
implementation("io.github.panpf.zoomimage:zoomimage-view-picasso:${LAST_VERSION}")

// Providing the basic ZoomImageView component, additional work is required to support network images and subsampling
implementation("io.github.panpf.zoomimage:zoomimage-view:${LAST_VERSION}")
```

### R8 / Proguard

ZoomImage's own obfuscation is already included in aar, but you may also need to add obfuscation
configuration for other libraries that depend indirectly

## Quickly Started

### compose android

The following is `SketchZoomAsyncImage`
For example, see the documentation for other components and detailed
usage [Get Started](docs/wiki/getstarted.md)

```kotlin
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

### compose multiplatform

```kotlin
val state: ZoomState by rememberZoomState()
LaunchedEffect(Unit) {
    state.subsampling.setImageSource(ImageSource.fromResource("huge_image.jpeg"))
}
ZoomImage(
    painter = painterResource("huge_image_thumbnail.jpeg"),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    state = state,
)
```

### view

The following is `SketchZoomImageView`
For example, see the documentation for other components and detailed
usage [Get Started](docs/wiki/getstarted.md)

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.displayImage("http://sample.com/sample.jpg")
```

## Document

* [Get Started](docs/wiki/getstarted.md)
* [Scale: scale, double-click scale, duration setting](docs/wiki/scale.md)
* [Offset: Move to the specified position](docs/wiki/offset.md)
* [Locate: Locate anywhere in the image and keeping it in the center of the screen](docs/wiki/locate.md)
* [Rotate: Rotate the image](docs/wiki/rotate.md)
* [Read Mode: Long images initially fill the screen for easy reading](docs/wiki/readmode.md)
* [Click: Receive click events](docs/wiki/click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM](docs/wiki/subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars to clarify the current scroll position](docs/wiki/scrollbar.md)
* [Log: Modify log level and output pipeline](docs/wiki/log.md)
* [Compose Multiplatform: Use on desktop platform](docs/wiki/multiplatform.md)

## Samples

You can find the sample code in
the [sample-android](sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples)
and [sample-desktop](sample-desktop/src/jvmMain/kotlin) module, or you can download the
APK、MSI、DMG、DEB package experience from the [release](https://github.com/panpf/zoomimage/releases)
page

## Changelog

Please review the [CHANGELOG](CHANGELOG.md) file

## My Projects

The following are my other open source projects. If you are interested, you can learn about them:

* [sketch](https://github.com/panpf/sketch)：A powerful and comprehensive image loader on Android,
  based entirely on coroutines, with support for GIFs, video thumbnails, and Compose
* [assembly-adapter](https://github.com/panpf/assembly-adapter)：A library on Android that provides
  multi-type Item implementations for various adapters. Incidentally, it also provides the most
  powerful divider for RecyclerView.
* [sticky-item-decoration](https://github.com/panpf/stickyitemdecoration)：RecyclerView sticky item
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