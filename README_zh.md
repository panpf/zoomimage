# ![logo_image] ZoomImage

![Platform][platform_image]
![Platform2][platform_image2]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

翻译：[English](README.md)

用于缩放图像的库，支持 Android View、Compose 以及 Compose
Multiplatform；支持双击缩放、单指或双指手势缩放、单指拖动、惯性滑动、定位、旋转、超大图子采样等功能。

https://github.com/panpf/zoomimage/assets/3250512/f067bed9-24e4-4ab8-a839-0731e155f4ef

## 特点

* `功能齐全`. 支持双击缩放、手势缩放、单指拖动、惯性滑动等基础功能
* `定位`. 支持定位到图片的任意位置并保持在屏幕中央
* `旋转`. 支持 0°, 90°, 180°, 270°, 360° 旋转图片
* `子采样`. 支持对超大图进行子采样显示，避免 OOM，碎片支持动画以及清晰度渐变
* `动态缩放比例`. 根据图片尺寸和容器尺寸自动计算出最合适的双击缩放比例
* `缩放阻尼`. 手动缩放超过最大或最小缩放比例后会有带阻尼感的橡皮筋效果
* `单指缩放`. 双击并按住屏幕上下滑动可缩放图像
* `滚动条`. 支持显示水平和垂直滚动条，明确当前滚动位置
* `阅读模式`. 阅读模式下显示长图时初始状态会自动充满屏幕，用户可立即开始阅读图片内容，省去用户双击放大的操作
* `Exif`. 支持读取 Exif Orientation 信息并自动旋转图片
* `图片加载器`. 提供对 sketch、coil、glide、picasso 等图片加载器的支持，也可以自定义支持更多图片加载器
* `Compose Multiplatform`. 支持 Compose Multiplatform，可在 Android、macOS、Windows、Linux 等平台使用

## Comparison of similar libraries/同类库对比

| Function/Library      | ZoomImage | [Telephoto] | [PhotoView] | [Subsampling<br/>ScaleImageView] |
|:----------------------|:---------:|:-----------:|:-----------:|:--------------------------------:|
| Compose               |     ✅     |      ✅      |      ❌      |                ❌                 |
| Compose Multiplatform |     ✅     |      ✅      |      ❌      |                ❌                 |
| View                  |     ✅     |      ❌      |      ✅      |                ✅                 |
| 旋转                    |     ✅     |      ❌      |      ✅      |                ❌                 |
| 定位                    |     ✅     |      ❌      |      ❌      |                ✅                 |
| 滚动条                   |     ✅     |      ❌      |      ❌      |                ❌                 |
| 阅读模式                  |     ✅     |      ❌      |      ❌      |                ❌                 |
| 子采样                   |     ✅     |      ✅      |      ❌      |                ✅                 |
| 子采样动画                 |     ✅     |      ❌      |      ❌      |                ❌                 |
| 单指缩放                  |     ✅     |      ✅      |      ✅      |                ✅                 |
| 动态缩放比例                |     ✅     |      ❌      |      ❌      |                ❌                 |
| 集成图片加载器               |     ✅     |      ✅      |      ❌      |                ❌                 |
| 丰富的交互接口               |     ✅     |      ❌      |      ✅      |                ✅                 |

### 多平台支持

| 平台/功能   | 缩放 | 子采样 | 集成图片加载框架 |
|:--------|:--:|:---:|:--------:|
| Android | ✅  |  ✅  |    ✅     |
| Desktop | ✅  |  ✅  |    ❌     |
| iOS     | 🚧 | 🚧  |    🚧    |
| Web     | 🚧 | 🚧  |    🚧    |

## 导入

`已发布到 mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (不包含 'v')

### compose android

`根据你用的图片加载器选择`

```kotlin
// 提供适配了 Sketch 图片加载器的 SketchZoomAsyncImage 组件，用法简单（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch:${LAST_VERSION}")

// 提供适配了 Coil 图片加载器的 CoilZoomAsyncImage 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil:${LAST_VERSION}")

// 提供适配了 Glide 图片加载器的 GlideZoomAsyncImage 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")
```

为什么没有 picasso 版本的 compose ZoomImage 组件？因为 Picasso 官方已经说明不会提供对 compose
的支持（[原文在此](https://github.com/square/picasso/issues/2203#issuecomment-826444442)）

### compose multiplatform

```kotlin
// 提供基础的 ZoomImage 组件，还需要做额外的工作以支持网络图片和子采样
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")
```

### view

`根据你用的图片加载器选择`

```kotlin
// 提供适配了 Sketch 图片加载器的 SketchZoomImageView 组件，用法简单（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch:${LAST_VERSION}")

// 提供适配了 Coil 图片加载器的 CoilZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-coil:${LAST_VERSION}")

// 提供适配了 Glide 图片加载器的 GlideZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-glide:${LAST_VERSION}")

// 提供适配了 Picasso 图片加载器的 PicassoZoomImageView 组件，用法简单
implementation("io.github.panpf.zoomimage:zoomimage-view-picasso:${LAST_VERSION}")

// 提供最基础的 ZoomImageView 组件，还需要做额外的工作以支持网络图片和子采样
implementation("io.github.panpf.zoomimage:zoomimage-view:${LAST_VERSION}")
```

### R8 / Proguard

ZoomImage 自己的混淆已经包含在了 aar 中，但你可能还需要为间接依赖的其它库添加混淆配置

## 快速上手

### compose android

下面以 `SketchZoomAsyncImage`
为例，其它组件以及详细用法请查看文档 [开始使用](docs/wiki/getstarted_zh.md)

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

下面以 `SketchZoomImageView`
为例，其它组件以及详细用法请查看文档 [开始使用](docs/wiki/getstarted_zh.md)

```kotlin
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/sample.jpg")
```

## 文档

* [开始使用](docs/wiki/getstarted_zh.md)
* [Scale: 缩放、双击缩放、时长设置](docs/wiki/scale_zh.md)
* [Offset: 移动到指定位置](docs/wiki/offset_zh.md)
* [Locate: 定位到图片的任意位置并保持在屏幕中央](docs/wiki/locate_zh.md)
* [Rotate: 旋转图片](docs/wiki/rotate_zh.md)
* [Read Mode: 长图初始时充满屏幕，方便阅读](docs/wiki/readmode_zh.md)
* [Click: 接收点击事件](docs/wiki/click_zh.md)
* [Subsampling: 对超大图进行子采样显示，避免 OOM](docs/wiki/subsampling_zh.md)
* [Scroll Bar: 显示水平和垂直滚动条，明确当前滚动位置](docs/wiki/scrollbar_zh.md)
* [Log: 修改日志等级以及输出管道](docs/wiki/log_zh.md)
* [Compose Multiplatform: 在桌面平台使用](docs/wiki/multiplatform_zh.md)

## 示例

你可以在 [sample-android](sample-android/src/main/java/com/github/panpf/zoomimage/sample/ui/examples)
和 [sample-desktop](sample-desktop/src/jvmMain/kotlin)
模块中找到示例代码，也可以到 [release](https://github.com/panpf/zoomimage/releases) 页面下载
APK、MSI、DMG、DEB 包体验

## 更新日志

请查看 [CHANGELOG](CHANGELOG_zh.md) 文件

## 我的项目

以下是我的其它开源项目，感兴趣的可以了解一下：

* [sketch](https://github.com/panpf/sketch)：Android 上的一个强大且全面的图片加载器，完全基于协程，还支持
  GIF、视频缩略图以及 Compose
* [assembly-adapter](https://github.com/panpf/assembly-adapter)：Android 上的一个为各种 Adapter 提供多类型
  Item 实现的库。还顺带为 RecyclerView 提供了最强大的 divider。
* [sticky-item-decoration](https://github.com/panpf/stickyitemdecoration)：RecyclerView 黏性 item 实现

## License

Apache 2.0. 有关详细信息，请参阅 [LICENSE](LICENSE.txt) 文件.

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