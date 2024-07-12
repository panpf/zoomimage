# ![logo_image] ZoomImage

![Platform][platform_image]
![Platform2][platform_image2]
[![API][min_api_image]][min_api_link]
[![License][license_image]][license_link]
[![version_icon]][version_link]

翻译：[English](README.md)

ZoomImage 是为 Compose Multiplatform 和 Android View 设计的用于手势缩放查看图像的库，它有以下特点和功能：

* `Compose Multiplatform`. 支持 Compose Multiplatform，可在 Android、macOS、Windows、Linux 等平台使用
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

https://github.com/panpf/zoomimage/assets/3250512/f067bed9-24e4-4ab8-a839-0731e155f4ef

### 多平台支持

| 功能/平台            | Android | iOS | Desktop | Web |
|:-----------------|:-------:|:---:|:-------:|:---:|
| 缩放               |    ✅    |  ✅  |    ✅    |  ✅  |
| 子采样              |    ✅    |  ✅  |    ✅    |  ✅  |
| Exif Orientation |    ✅    |  ✅  |    ✅    |  ✅  |
| 集成 [Sketch]      |    ✅    |  ✅  |    ✅    |  ✅  |
| 集成 [Coil]        |    ✅    |  ✅  |    ✅    |  ✅  |
| 集成 [Glide]       |    ✅    |  ❌  |    ❌    |  ❌  |
| 集成 [Picasso]     |    ✅    |  ❌  |    ❌    |  ❌  |

## 下载

`已发布到 mavenCentral`

`${LAST_VERSION}`: [![Download][version_icon]][version_link] (不包含 'v')

Compose multiplatform：

```kotlin
// 提供适配了 Sketch 图片加载器的 SketchZoomAsyncImage 组件（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch:${LAST_VERSION}")

// 提供适配了旧版 Sketch 3 图片加载器的 SketchZoomAsyncImage 组件
implementation("io.github.panpf.zoomimage:zoomimage-compose-sketch3:${LAST_VERSION}")

// 提供适配了 Coil 图片加载器的 CoilZoomAsyncImage 组件
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil:${LAST_VERSION}")

// 提供适配了旧版 Coil 2 图片加载器的 CoilZoomAsyncImage 组件
implementation("io.github.panpf.zoomimage:zoomimage-compose-coil2:${LAST_VERSION}")

// 提供基础的 ZoomImage 组件，还需要做额外的工作以支持子采样，不支持网络图片
implementation("io.github.panpf.zoomimage:zoomimage-compose:${LAST_VERSION}")

// 支持从 composeResources 文件夹下加载图片
implementation("io.github.panpf.zoomimage:zoomimage-compose-resources:${LAST_VERSION}")
```

> [!TIP]
> 根据你用的图片加载器或需求任选其一即可

Only android compose：

```kotlin
// 提供适配了 Glide 图片加载器的 GlideZoomAsyncImage 组件
implementation("io.github.panpf.zoomimage:zoomimage-compose-glide:${LAST_VERSION}")
```

> [!TIP]
> 为什么没有 picasso 版本的 compose ZoomImage 组件？因为 Picasso 官方已经说明不会提供对 compose
> 的支持（[原文在此][picasso_compose_post]）

Android view：

```kotlin
// 提供适配了 Sketch 图片加载器的 SketchZoomImageView 组件（推荐使用）
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch:${LAST_VERSION}")

// 提供适配了旧版 Sketch 3 图片加载器的 SketchZoomImageView 组件
implementation("io.github.panpf.zoomimage:zoomimage-view-sketch3:${LAST_VERSION}")

// 提供适配了 Coil 图片加载器的 CoilZoomImageView 组件
implementation("io.github.panpf.zoomimage:zoomimage-view-coil:${LAST_VERSION}")

// 提供适配了旧版 Coil 2 图片加载器的 CoilZoomImageView 组件
implementation("io.github.panpf.zoomimage:zoomimage-view-coil2:${LAST_VERSION}")

// 提供适配了 Glide 图片加载器的 GlideZoomImageView 组件
implementation("io.github.panpf.zoomimage:zoomimage-view-glide:${LAST_VERSION}")

// 提供适配了 Picasso 图片加载器的 PicassoZoomImageView 组件
implementation("io.github.panpf.zoomimage:zoomimage-view-picasso:${LAST_VERSION}")

// 提供最基础的 ZoomImageView 组件，还需要做额外的工作以支持子采样，不支持网络图片
implementation("io.github.panpf.zoomimage:zoomimage-view:${LAST_VERSION}")
```

> [!TIP]
> 根据你用的图片加载器或需求任选其一即可

#### R8 / Proguard

ZoomImage 自己的混淆已经包含在了 aar 中，但你可能还需要为间接依赖的其它库添加混淆配置

## 快速上手

Compose multiplatform：

```kotlin
// 使用基础的 ZoomImage 组件
val zoomState: ZoomState by rememberZoomState()
LaunchedEffect(Unit) {
    val resUri = Res.getUri("files/huge_world.jpeg")
    zoomState.subsampling.setImageSource(ImageSource.fromComposeResource(resUri))
}
ZoomImage(
    painter = painterResource(Res.drawable.huge_world_thumbnail),
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
    zoomState = zoomState,
)

// 使用 SketchZoomAsyncImage 组件
SketchZoomAsyncImage(
    uri = "http://sample.com/huge_world.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)

// 使用 CoilZoomAsyncImage 组件
CoilZoomAsyncImage(
    model = "http://sample.com/huge_world.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> SketchZoomAsyncImage 和 CoilZoomAsyncImage 的用法和它们原本的 AsyncImage
> 一样，只是多了一个 `zoomState: ZoomState` 参数

Only android compose：

```kotlin
// 使用 GlideZoomAsyncImage 组件
GlideZoomAsyncImage(
    model = "http://sample.com/huge_world.jpeg",
    contentDescription = "view image",
    modifier = Modifier.fillMaxSize(),
)
```

> [!TIP]
> GlideZoomAsyncImage 的用法和它原本的 GlideImage 一样，只是多了一个 `zoomState: ZoomState` 参数

Android view：

```kotlin
// 使用基础的 ZoomImageView 组件
val zoomImageView = ZoomImageView(context)
zoomImageView.setImageResource(R.drawable.huge_world_thumbnail)
zoomImageView.subsampling.setImageSource(ImageSource.resource(R.raw.huge_world))

// 使用 SketchZoomAsyncImage 组件
val sketchZoomImageView = SketchZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/huge_world.jpeg")

// 使用 CoilZoomImageView 组件
val coilZoomImageView = CoilZoomImageView(context)
sketchZoomImageView.loadImage("http://sample.com/huge_world.jpeg")

// 使用 GlideZoomImageView 组件
val glideZoomImageView = GlideZoomImageView(context)
Glide.with(this@GlideZoomImageViewFragment)
    .load("http://sample.com/huge_world.jpeg")
    .into(glideZoomImageView)

// 使用 PicassoZoomImageView 组件
val picassoZoomImageView = PicassoZoomImageView(context)
picassoZoomImageView.loadImage("http://sample.com/huge_world.jpeg")
```

## 文档

* [开始使用](docs/wiki/getstarted_zh.md)
* [Zoom: 缩放、双击缩放、时长设置](docs/wiki/zoom_zh.md)
* [Offset: 移动到指定位置](docs/wiki/offset_zh.md)
* [Locate: 定位到图片的任意位置](docs/wiki/locate_zh.md)
* [Rotate: 旋转图片](docs/wiki/rotate_zh.md)
* [Read Mode: 长图初始时充满屏幕，方便阅读](docs/wiki/readmode_zh.md)
* [Click: 接收点击事件](docs/wiki/click_zh.md)
* [Subsampling: 对超大图进行子采样显示，避免 OOM](docs/wiki/subsampling_zh.md)
* [Scroll Bar: 显示水平和垂直滚动条](docs/wiki/scrollbar_zh.md)
* [Log: 修改日志等级以及输出管道](docs/wiki/log_zh.md)

## 示例

你可以在 [examples](sample/src/commonMain/kotlin/com/github/panpf/zoomimage/sample/ui/examples)
目录下找到示例代码，也可以到 [release](https://github.com/panpf/zoomimage/releases) 页面下载 App 体验

## 更新日志

请查看 [CHANGELOG](CHANGELOG_zh.md) 文件

## 运行示例 App

准备环境：

1. Android Studio: Koala+ (2024.1.1+)
2. JDK: 17+
3. 使用 [kdoctor] 检查运行环境，并按照提示安装需要的软件
4. Android Studio 安装 `Kotlin Multiplatform` 和 `Compose Multiplatform IDE Support` 插件

运行示例 App：

1. 克隆项目并使用 Android Studio 打开
2. `.run` 目录下已经添加了各个平台的运行配置，同步完成后直接在 Android Studio
   顶部运行配置下拉框中选择对应平台的运行配置然后点击运行即可
3. ios 平台的运行配置需要你根据模版手动创建，如下：
    1. 拷贝 `.run/iosSample.run.template.xml` 文件，并去掉 `.template` 后缀，`.ignore`
       文件中已经配置了忽略 `iosSample.run.xml`
    2. 在顶部运行配置下拉框点击 `Edit Configurations` 选择 `iosSample` 然后配置 `Execute target` 即可

## 同类库对比

| Function/Library      | ZoomImage | [Telephoto] | [PhotoView] | [Subsampling<br/>ScaleImageView] |
|:----------------------|:---------:|:-----------:|:-----------:|:--------------------------------:|
| Compose Multiplatform |     ✅     |      ✅      |      ❌      |                ❌                 |
| Android Compose       |     ✅     |      ✅      |      ❌      |                ❌                 |
| Android View          |     ✅     |      ❌      |      ✅      |                ✅                 |
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

## 我的项目

以下是我的其它开源项目，感兴趣的可以了解一下：

* [sketch](https://github.com/panpf/sketch)：Sketch 是一个为 Compose Multiplatform 和 Android View
  设计的图像加载库。它功能强大且丰富，除了基本功能外，还支持GIF、SVG、视频缩略图、Exif Orientation 等。
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

[Sketch]: https://github.com/panpf/sketch

[Coil]: https://github.com/coil-kt/coil

[Glide]: https://github.com/bumptech/glide

[Picasso]: https://github.com/square/picasso

[picasso_compose_post]: https://github.com/square/picasso/issues/2203#issuecomment-826444442

[kdoctor]: https://github.com/Kotlin/kdoctor