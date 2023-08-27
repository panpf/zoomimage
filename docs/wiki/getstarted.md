## 开始使用

> * The following example takes precedence over the Compose version of the ZoomImage component for demonstration
> * The API of ZoomImageView is exactly the same as ZoomImage, except that the entrance is different
> * ZoomState.zoomable is equivalent to ZoomImageView.zoomAbility
> * ZoomState.subsampling is equivalent to ZoomImageView.subsamplingAbility
    <br>-----------</br>
> * 以下示例优先用 Compose 版本的 ZoomImage 组件来演示
> * ZoomImageView 的 API 和 ZoomImage 一模一样，只是入口不一样
> * ZoomState.zoomable 等价于 ZoomImageView.zoomAbility
> * ZoomState.subsampling 等价于 ZoomImageView.subsamplingAbility

### Components/组件

zoomimage 库包含了多个组件可供选择，你可以根据自己的需求选择合适的组件。

*不同的组件需要导入不同的依赖，请参考 [README] 导入对应的依赖*

compose：

* [SketchZoomAsyncImage]：集成了 [Sketch] 图片加载库的缩放 Image 组件`（推荐使用）`
    * 用法和 [Sketch] 的 [AsyncImage][SketchAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomAsyncImageSample]
* [CoilZoomAsyncImage]：集成了 [Coil] 图片加载库的缩放 Image 组件
    * 用法和 [Coil] 的 [AsyncImage][CoilAsyncImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomAsyncImageSample]
* [GlideZoomAsyncImage]：集成了 [Glide] 图片加载库的缩放 Image 组件
    * 用法和 [Glide] 的 [GlideImage] 组件一样
    * 已支持网络图片和子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomAsyncImageSample]
* [ZoomImage]：最基础的缩放 Image 组件，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageSample]

view：

* [SketchZoomImageView]：集成了 [Sketch] 图片加载库的缩放 ImageView`（推荐使用）`
    * 已适配 [Sketch] 支持子采样，无需做任何额外的工作
    * 参考示例 [SketchZoomImageViewFragment]
* [CoilZoomImageView]：集成了 [Coil] 图片加载库的缩放 ImageView
    * 已适配 [Coil] 支持子采样，无需做任何额外的工作
    * 参考示例 [CoilZoomImageViewFragment]
* [GlideZoomImageView]：集成了 [Glide] 图片加载库的缩放 ImageView
    * 已适配 [Glide] 支持子采样，无需做任何额外的工作
    * 参考示例 [GlideZoomImageViewFragment]
* [PicassoZoomImageView]：集成了 [Picasso] 图片加载库的缩放 ImageView
    * 已适配 [Picasso] 支持子采样，无需做任何额外的工作
    * 参考示例 [PicassoZoomImageViewFragment]
* [ZoomImageView]：最基础的缩放 ImageView，未集成图片加载库
    * 还需要做额外的工作以支持网络图片和子采样
    * 参考示例 [ZoomImageViewFragment]

总结：

* 集成了图片加载器的组件无需任何额外的工作即可支持任意来源的图片和子采样功能
* 未集成图片加载器的组件只能显示本地图片，以及需要额外调用 setImageSource() 方法以开启子采样功能

## 使用

### Compose

[//]: # (todo contine)


### 获取相关信息

baseTransform
userTransform
transform
transform.scale
transform.offset
transform.rotation
minScale
mediumScale
maxScale
transforming
contentBaseDisplayRect
contentBaseVisibleRect
contentDisplayRect
contentVisibleRect
scrollEdge
containerSize
contentSize
contentOriginSize

ZoomImageView 设置 contentScale 和 alignment

## Document/文档

* [Scale: scale, double-click scale, duration setting/缩放、双击缩放、时长设置](scale.md)
* [Offset: Move to the specified position/移动到指定位置](offset.md)
* [Location: Moves the specified location of the picture to the middle of the screen/将图片的指定位置移动到屏幕中间](location.md)
* [Rotate: Rotate the image/旋转图片](rotate.md)
* [Read Mode: Automatically fills the screen for easy reading/自动充满屏幕，方便阅读](readmode.md)
* [Click: Receive click events/接收点击事件](click.md)
* [Subsampling: Subsampling the display of huge image to avoid OOM/对超大图进行子采样显示，避免 OOM](subsampling.md)
* [Scroll Bar: Displays horizontal and vertical scroll bars to clarify the current scroll position/显示水平和垂直滚动条，明确当前滚动位置](scrollbar.md)
* [Log/日志](log.md)

[ZoomImage]: ../../zoomimage-compose/src/main/java/com/github/panpf/zoomimage/ZoomImage.kt

[CoilZoomAsyncImage]: ../../zoomimage-compose-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomAsyncImage.kt

[GlideZoomAsyncImage]: ../../zoomimage-compose-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomAsyncImage.kt

[SketchZoomAsyncImage]: ../../zoomimage-compose-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomAsyncImage.kt


[ZoomImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/ZoomImageSample.kt

[CoilZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/CoilZoomAsyncImageSample.kt

[GlideZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/GlideZoomAsyncImageSample.kt

[SketchZoomAsyncImageSample]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/compose/SketchZoomAsyncImageSample.kt


[ZoomImageView]: ../../zoomimage-view/src/main/java/com/github/panpf/zoomimage/ZoomImageView.kt

[CoilZoomImageView]: ../../zoomimage-view-coil/src/main/java/com/github/panpf/zoomimage/CoilZoomImageView.kt

[GlideZoomImageView]: ../../zoomimage-view-glide/src/main/java/com/github/panpf/zoomimage/GlideZoomImageView.kt

[PicassoZoomImageView]: ../../zoomimage-view-picasso/src/main/java/com/github/panpf/zoomimage/PicassoZoomImageView.kt

[SketchZoomImageView]: ../../zoomimage-view-sketch/src/main/java/com/github/panpf/zoomimage/SketchZoomImageView.kt


[ZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/ZoomImageViewFragment.kt

[CoilZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/CoilZoomImageViewFragment.kt

[GlideZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/GlideZoomImageViewFragment.kt

[PicassoZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/PicassoZoomImageViewFragment.kt

[SketchZoomImageViewFragment]: ../../sample/src/main/java/com/github/panpf/zoomimage/sample/ui/examples/view/SketchZoomImageViewFragment.kt

[Sketch]: https://github.com/panpf/sketch

[SketchAsyncImage]: https://github.com/panpf/sketch/blob/main/sketch-compose/src/main/java/com/github/panpf/sketch/compose/AsyncImage.kt

[Coil]: https://github.com/coil-kt/coil

[CoilAsyncImage]: https://github.com/coil-kt/coil/blob/main/coil-compose-singleton/src/main/java/coil/compose/SingletonAsyncImage.kt

[Glide]: https://github.com/bumptech/glide

[GlideImage]: https://github.com/bumptech/glide/blob/master/integration/compose/src/main/java/com/bumptech/glide/integration/compose/GlideImage.kt

[Picasso]: https://github.com/square/picasso

[README]: ../../README.md