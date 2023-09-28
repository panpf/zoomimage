# ![logo_image] ZoomImage

![Platform][platform_image]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

Android library for scaling images, supporting double-tap zoom, gesture zoom, single-finger drag,
inertial swipe, locate, rotate, huge image sub-sampling, and more. Both View and Compose
are supported.
<br>-----------------</br>
用于缩放图像的 Android 库，支持双击缩放、手势缩放、单指拖动、惯性滑动、定位、旋转、超大图子采样等功能。支持
View 和 Compose 两种方式。

https://github.com/panpf/zoomimage/assets/3250512/f067bed9-24e4-4ab8-a839-0731e155f4ef

## Features/特点

* `Complete`. Support basic functions such as double-click zoom, gesture zoom, single-finger drag,
  and inertial swipe
* `Locate`. Support for locate anywhere in the image and keeping it in the center of the screen
* `Rotate`. Supports 0°, 90°, 180°, 270°, 360° rotation of pictures
* `Subsampling`. Support for subsampling of very large images to avoid OOM, tile support animation,
  and sharpness gradients
* `Dynamic scaling`. Automatically calculates the most appropriate double-click scaling factor based
  on image size and container size
* `Scaling damping`. When manually scaled beyond the maximum or minimum zoom factor, there is a
  damped rubber band effect
* `Scroll bar`. Supports displaying horizontal and vertical scroll bars to clarify the current
  scroll position
* `Read Mode`. When a long image is displayed in reading mode, the initial state automatically fills
  the screen, and the user can immediately start reading the image content, eliminating the need for
  the user to double-click to zoom in
* `Image Loader`. Provide support for image loaders such as sketch, coil, glide, picasso, etc., and
  can also customize support for more image loaders

<div>-----------------</div>

* `功能齐全`. 支持双击缩放、手势缩放、单指拖动、惯性滑动等基础功能
* `定位`. 支持定位到图片的任意位置并保持在屏幕中央
* `旋转`. 支持 0°, 90°, 180°, 270°, 360° 旋转图片
* `子采样`. 支持对超大图进行子采样显示，避免 OOM，碎片支持动画以及清晰度渐变
* `动态缩放`. 根据图片尺寸和容器尺寸自动计算出最合适的双击缩放比例
* `缩放阻尼`. 手动缩放超过最大或最小缩放比例后会有带阻尼感的橡皮筋效果
* `滚动条`. 支持显示水平和垂直滚动条，明确当前滚动位置
* `阅读模式`. 阅读模式下显示长图时初始状态会自动充满屏幕，用户可立即开始阅读图片内容，省去用户双击放大的操作
* `图片加载器`. 提供对 sketch、coil、glide、picasso 等图片加载器的支持，也可以自定义支持更多图片加载器

## Comparison of similar libraries/同类库对比

| Function/Library            | ZoomImage | [Telephoto] | [PhotoView] | [Subsampling<br/>ScaleImageView] |
|:----------------------------|:---------:|:-----------:|:-----------:|:--------------------------------:|
| Compose                     |     ✅     |      ✅      |      ❌      |                ❌                 |
| View                        |     ✅     |      ❌      |      ✅      |                ✅                 |
| Subsampling/在采样             |     ✅     |      ✅      |      ❌      |                ✅                 |
| Rotate/旋转                   |     ✅     |      ❌      |      ✅      |                ❌                 |
| Locate/定位                   |     ✅     |      ❌      |      ❌      |                ❌                 |
| Read Mode/阅读模式              |     ✅     |      ❌      |      ❌      |                ❌                 |
| Scroll Bar/滚动条              |     ✅     |      ❌      |      ❌      |                ❌                 |
| Image Loader/集成图片加载器        |     ✅     |      ✅      |      ❌      |                ❌                 |
| Dynamic scaling/动态缩放比例      |     ✅     |      ❌      |      ❌      |                ❌                 |
| Subsampling animation/子采样动画 |     ✅     |      ❌      |      ❌      |                ❌                 |
| Rich interfaces/丰富的交互接口     |     ✅     |      ❌      |      ✅      |                ✅                 |

## Import/导入

`Published to mavenCentral · 已发布到 mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (Not included 'v' · 不包含 'v')

### compose

`Choose one of the following modules · 以下模块任选其一即可`

```kotlin
// The SketchZoomAsyncImage component is provided with the Coil Image Loader, easy to use (recommended)
// 提供适配了 Sketch 图片加载器的 SketchZoomAsyncImage 组件，用法简单（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch:${LAST_VERSION}")

// The CoilZoomAsyncImage component is provided with the Coil Image Loader, easy to use
// 提供适配了 Coil 图片加载器的 CoilZoomAsyncImage 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil:${LAST_VERSION}")

// The GlideZoomAsyncImage component is provided with the Coil Image Loader, easy to use
// 提供适配了 Glide 图片加载器的 GlideZoomAsyncImage 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")

// Providing the most basic ZoomImage component, there is still a lot of work to be done to use it, 
// additional work needs to be done to support network image and subsampling
// 提供最基础的 ZoomImage 组件，还需要做额外的工作以支持网络图片和子采样
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")
```

Why is there no picasso version of the compose ZoomImage component? Picasso has officially stated
that it will not provide compose
Support ([sic](https://github.com/square/picasso/issues/2203#issuecomment-826444442))
<br>-----------------</br>
为什么没有 picasso 版本的 compose ZoomImage 组件？因为 Picasso 官方已经说明不会提供对 compose
的支持（[原文在此](https://github.com/square/picasso/issues/2203#issuecomment-826444442)）

### view

`Choose one of the following modules · 以下模块任选其一即可`

```kotlin
// The SketchZoomImageView component is provided with the Sketch Image Loader, easy to use (recommended)
// 提供适配了 Sketch 图片加载器的 SketchZoomImageView 组件，用法简单（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Coil Image Loader, easy to use
// 提供适配了 Coil 图片加载器的 CoilZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-coil:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Glide Image Loader, easy to use
// 提供适配了 Glide 图片加载器的 GlideZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-glide:${LAST_VERSION}")

// The SketchZoomImageView component is provided with the Picasso Image Loader, easy to use
// 提供适配了 Picasso 图片加载器的 PicassoZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-picasso:${LAST_VERSION}")

// Providing the most basic ZoomImageView component, there is still a lot of work to be done to use it, 
// additional work needs to be done to support network image and subsampling
// 提供最基础的 ZoomImageView 组件，还需要做额外的工作以支持网络图片和子采样
implementation("io.github.panpf.zoomimage:zoomimage-view:${LAST_VERSION}")
```

### R8 / Proguard

ZoomImage's own obfuscation is already included in aar, but you may also need to add obfuscation
configuration for other libraries that depend indirectly
<br>-----------------</br>
ZoomImage 自己的混淆已经包含在了 aar 中，但你可能还需要为间接依赖的其它库添加混淆配置

## Quickly Started/快速上手

### compose

The following is `SketchZoomAsyncImage`
For example, see the documentation for other components and detailed
usage [Get Started](docs/wiki/getstarted.md)
<br>-----------------</br>
下面以 `SketchZoomAsyncImage`
为例，其它组件以及详细用法请查看文档 [开始使用](docs/wiki/getstarted.md)

```kotlin
SketchZoomAsyncImage(
    imageUri = "http://sample.com/sample.jpg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

### view

The following is `SketchZoomImageView`
For example, see the documentation for other components and detailed
usage [Get Started](docs/wiki/getstarted.md)
<br>-----------------</br>
下面以 `SketchZoomImageView`
为例，其它组件以及详细用法请查看文档 [开始使用](docs/wiki/getstarted.md)

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.displayImage("http://sample.com/sample.jpg")
```

## Document/文档

* [Get Started/开始使用](docs/wiki/getstarted.md)
* [Scale: scale, double-click scale, duration setting/缩放、双击缩放、时长设置](docs/wiki/scale.md)
* [Offset: Move to the specified position/移动到指定位置](docs/wiki/offset.md)
* [Locate: Locate anywhere in the image and keeping it in the center of the screen/定位到图片的任意位置并保持在屏幕中央](docs/wiki/locate.md)
* [Rotate: Rotate the image/旋转图片](docs/wiki/rotate.md)
* [Read Mode: Automatically fills the screen for easy reading/自动充满屏幕，方便阅读](docs/wiki/readmode.md)
* [Click: Receive click events/接收点击事件](docs/wiki/click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM/对超大图进行子采样显示，避免 OOM](docs/wiki/subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars to clarify the current scroll position/显示水平和垂直滚动条，明确当前滚动位置](docs/wiki/scrollbar.md)
* [Log/日志](docs/wiki/log.md)

## Samples/示例

You can find the sample code in
the [sample](https://github.com/panpf/zoomimage/tree/main/sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples)
module, or you can download the APK experience
from the [release](https://github.com/panpf/zoomimage/releases) page
<br>-----------------</br>
你可以在 [sample](https://github.com/panpf/zoomimage/tree/main/sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples)
模块中找到示例代码，也可以到 [release](https://github.com/panpf/zoomimage/releases) 页面下载 APK 体验

## Changelog/更新日志

Please review the [CHANGELOG.md] file
<br>-----------------</br>
请查看 [CHANGELOG.md] 文件

## My Projects/我的项目

以下是我的其它开源项目，感兴趣的可以了解一下：

* [sketch](https://github.com/panpf/sketch)：A powerful and comprehensive image loader on Android,
  based entirely on coroutines, with support for GIFs, video thumbnails, and Compose
* [assembly-adapter](https://github.com/panpf/assembly-adapter)：A library on Android that provides
  multi-type Item implementations for various adapters. Incidentally, it also provides the most
  powerful divider for RecyclerView.
* [sticky-item-decoration](https://github.com/panpf/stickyitemdecoration)：RecyclerView sticky item
  implementation
  <br>-----------------</br>
* [sketch](https://github.com/panpf/sketch)：Android 上的一个强大且全面的图片加载器，完全基于协程，还支持
  GIF、视频缩略图以及 Compose
* [assembly-adapter](https://github.com/panpf/assembly-adapter)：Android 上的一个为各种 Adapter 提供多类型
  Item 实现的库。还顺带为 RecyclerView 提供了最强大的 divider。
* [sticky-item-decoration](https://github.com/panpf/stickyitemdecoration)：RecyclerView 黏性 item 实现

## License

    Copyright (C) 2023 panpf <panpfpanpf@outlook.com>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[logo_image]: docs/res/logo_mini.png

[platform_image]: https://img.shields.io/badge/Platform-Android-brightgreen.svg

[license_image]: https://img.shields.io/badge/License-Apache%202-blue.svg

[license_link]: https://www.apache.org/licenses/LICENSE-2.0

[version_icon]: https://img.shields.io/maven-central/v/io.github.panpf.zoomimage/zoomimage-compose

[version_link]: https://repo1.maven.org/maven2/io/github/panpf/zoomimage/

[min_api_image]: https://img.shields.io/badge/API-16%2B-orange.svg

[min_api_link]: https://android-arsenal.com/api?level=16


[ZoomImage]: zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: zoomimage-compose-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: zoomimage-compose-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: zoomimage-compose-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomAsyncImage.kt


[ZoomImageSample]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/ZoomImageSample.kt

[CoilZoomAsyncImageSample]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/CoilZoomAsyncImageSample.kt

[GlideZoomAsyncImageSample]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/SketchZoomAsyncImageSample.kt


[ZoomImageView]: zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: zoomimage-view-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: zoomimage-view-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: zoomimage-view-picasso/src/main/java/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: zoomimage-view-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomImageView.kt


[ZoomImageViewFragment]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/ZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/SketchZoomImageViewFragment.kt

[Telephoto]: https://github.com/saket/telephoto

[PhotoView]: https://github.com/Baseflow/PhotoView

[Subsampling<br/>ScaleImageView]: https://github.com/davemorrissey/subsampling-scale-image-view

[CHANGELOG.md]: CHANGELOG.md